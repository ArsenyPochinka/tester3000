# tester3000

Сквозной регресс авторизаций: формирует сообщения по выбранным тест-кейсам из БД, отправляет их во внешние контуры (m210 HTTP + Kafka clearing), слушает outbox-события и фиксирует шаги в PostgreSQL. Управление — через Swagger API.

---

## Что делает приложение

1. Принимает параметры карты и список кодов тестов.
2. Берёт исходные auth/clr JSON из таблицы `test_messages_from_25`.
3. Маппит их на контракты:
   - авторизация → **m210** (`ProductAuthorizationRequest`);
   - клиринг → **m095** (`Inbound`).
4. Параметризует `card`, генерирует `rId`, общий `match.key` / `link.key`, обновляет даты (кроме `expDate`).
5. Сохраняет подготовленные сообщения в `regression_test_message`.
6. Сразу возвращает `runId` и код запуска (`0` = принят).
7. В фоне:
   - шлёт auth в m210;
   - через `clearing-delay-ms` публикует clearing в Kafka;
   - (опционально) публикует заглушечные outbox-события;
   - пишет результаты в `process`.
8. Слушает три outbox-топика и на каждое событие добавляет строку в `process`.
9. По `runId` отдаёт HTML-отчёт (собирается на лету из БД).

---

## Стек

| Компонент | Версия / технология |
|-----------|---------------------|
| Java | 21 |
| Spring Boot | 3.4.12 |
| БД | PostgreSQL 16 + Liquibase |
| Messaging | Apache Kafka (Confluent cp-kafka 7.6.1, KRaft) |
| API docs | springdoc OpenAPI (Swagger UI) |
| Сборка | Maven |
| Запуск | Docker Compose |

---

## Быстрый старт

```bash
mvn -DskipTests package
docker compose up --build -d
```

| Сервис | Порт | Назначение |
|--------|------|------------|
| `tester3000` | `127.0.0.1:8080` | API + Swagger |
| `postgres` | `127.0.0.1:5432` | БД `tester3000` / user `tester` / password `tester` |
| `kafka` | `127.0.0.1:9092` | Kafka |

Порты слушаются только на localhost (не торчат в LAN).

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

```bash
docker compose down
```

Логи прогонов — только в stdout пода (`[runId=…] [reqId=…]`).

Образ собирается из уже собранного jar (`Dockerfile`), поэтому перед `docker compose up --build` нужен `mvn package`.

---

## Как пользоваться

### 1. Запуск регресса

**`POST /api/v1/regression/run`**

```json
{
  "card": {
    "auth": { "presence": true },
    "plasticId": "15c812a5-edc4-4dd2-a842-6fd53be44369",
    "cardId": "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a",
    "expDate": "2030-01-01T00:00:00.000",
    "emv": { "mbr": 90 }
  },
  "tests": [
    "CASH",
    "DEPOSIT",
    "GOODS",
    "GOODS_WITH_CASHBACK",
    "PAYMENT_CREDIT",
    "PAYMENT_DEBIT"
  ]
}
```

- `card` — параметры карты для параметризации сообщений.
- `tests` — коды из БД. В Swagger по умолчанию подставляются все коды на момент старта приложения. Запускаются только выбранные.

Ответ:

```json
{
  "runId": "1f38ff37-b952-4de5-9872-b0a473d920a6",
  "code": 0
}
```

| Поле | Смысл |
|------|--------|
| `runId` | Идентификатор прогона |
| `code` | `0` — запуск принят; дальше — фон |

Неизвестный код / пустой `tests` / невалидная карта → `400`.

```bash
curl -s -X POST http://localhost:8080/api/v1/regression/run \
  -H 'Content-Type: application/json' \
  -d '{
    "card": {
      "auth": { "presence": true },
      "plasticId": "15c812a5-edc4-4dd2-a842-6fd53be44369",
      "cardId": "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a",
      "expDate": "2030-01-01T00:00:00.000",
      "emv": { "mbr": 90 }
    },
    "tests": ["CASH", "GOODS"]
  }'
```

### 2. HTML-отчёт

**`GET /api/v1/regression/report/{runId}`**

В браузере: `http://localhost:8080/api/v1/regression/report/<runId>`

Отчёт не сохраняется на диск — каждый раз собирается из `regression_test_message` и `process`.  
Несуществующий `runId` → `404`.

### 3. Логи

Только stdout приложения (логи пода). Сквозная идентификация:

```text
[runId=…] [reqId=…] ОТПРАВЛЕНО → m210 POST …: {payload}
[runId=…] [reqId=…] ПОЛУЧЕНО ← m210 HTTP 200: {body}
[runId=…] [reqId=…] ПОЛУЧЕНО ← 25_FIN_MESSAGE status=Approved …: {payload}
```

Чужие события outbox (без связки с нашими `reqId`) вычитываются и игнорируются без логов.

---

## Как устроен прогон

```text
POST /run
    │
    ├─ чтение выбранных тестов из test_messages_from_25
    ├─ маппинг auth → m210, clr → m095 + параметризация card
    ├─ запись в regression_test_message
    ├─ ответ { runId, code: 0 }
    │
    └─ async (на каждый тест):
           AUTH_SEND      → HTTP m210
           pause clearing-delay-ms
           CLEARING_SEND  → Kafka ccop.prx.event.clearing
           stub (если вкл.) → по 2 сообщения в 3 outbox-топика
           consumers      → строки process (25 / 104 / 39)
```

### Связка сообщений

- У auth и clearing свой `rId` (`reqId` в process).
- Общий ключ: `match.key` (auth) = `link[].key` (clearing) — 40 символов `[A-Z0-9]` + 18-значный RRN.
- Outbox матчится по `Object.Id.Id` = `reqId`.

### Шаги `process`

| step | Смысл |
|------|--------|
| `AUTH_SEND` | Отправка авторизации в m210 |
| `CLEARING_SEND` | Публикация клиринга в Kafka |
| `25_FIN_MESSAGE` | `tsss.ccop_incoming_fin_message.outbox` |
| `104_FIN_INSTRUCTION` | `tsss.ccop_fin_instruction.outbox` |
| `39_FIN_TRANSACTION` | `tsss.ccop_fin_transaction.outbox` |

Поля строки: `type` (`AUTH`/`CLR`), `test_name`, `status`, `result`, `description`.  
На каждое outbox-событие — новая запись.

---

## Каталог тестов

Таблица `test_messages_from_25` — единственный источник кейсов.

| Колонка | Тип | Смысл |
|---------|-----|--------|
| `test_code` | varchar PK | Код теста |
| `test_description` | text | Описание |
| `auth` | jsonb | Auth-сообщение |
| `clr` | jsonb | Clearing (может быть NULL) |

| test_code | Описание |
|-----------|----------|
| `CASH` | Снятие наличных |
| `DEPOSIT` | Внесение наличных |
| `GOODS` | Покупка товаров |
| `GOODS_WITH_CASHBACK` | Покупка товаров с кэшбэком |
| `PAYMENT_DEBIT` | Дебетовый платёж |
| `PAYMENT_CREDIT` | Кредитовый платёж |

Новый тест — insert в таблицу (или Liquibase seed) и рестарт приложения, если нужен обновлённый default-список в Swagger.

---

## Таблицы результатов

### `regression_test_message`

Подготовленные сообщения прогона: `id`, `run_id`, `test_name`, `auth_message`, `clearing_message`, `created_at`.

### `process`

Журнал шагов. Связь: `run_id` + `test_message_id` → `regression_test_message`.

---

## Конфигурация

| Параметр | Env | Default | Назначение |
|----------|-----|---------|------------|
| `tester3000.clearing-delay-ms` | `CLEARING_DELAY_MS` | `2000` | Пауза auth → clearing |
| `tester3000.m210.base-url` | `M210_BASE_URL` | `http://127.0.0.1:8080` | URL m210 |
| `tester3000.m210.stub-enabled` | `M210_STUB_ENABLED` | `true` | Встроенная заглушка m210 |
| `tester3000.kafka.fin-outbox-stub-enabled` | `FIN_OUTBOX_STUB_ENABLED` | `true` | Заглушка outbox (3×2) |
| DB | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | localhost / tester3000 / tester | PostgreSQL |
| Kafka | `KAFKA_BOOTSTRAP` | `localhost:9092` | Bootstrap |

### Заглушки

По умолчанию:

1. **m210** — `POST /api/1.1.0/prod_auth/_request` в том же приложении.
2. **Outbox** — после auth/clearing по 2 сообщения со случайным статусом в:
   - `tsss.ccop_incoming_fin_message.outbox`
   - `tsss.ccop_fin_instruction.outbox`
   - `tsss.ccop_fin_transaction.outbox`

Реальный контур:

```bash
M210_BASE_URL=https://real-m210-host \
M210_STUB_ENABLED=false \
FIN_OUTBOX_STUB_ENABLED=false \
docker compose up --build -d
```

---

## Локальный запуск (только infra в Docker)

```bash
docker compose up -d postgres kafka
mvn spring-boot:run
```

Или:

```bash
mvn -DskipTests package
java -jar target/tester3000-1.0.0-SNAPSHOT.jar
```

---

## Контракты

Единственный источник JSON Schema: `contract/schemas/`

- `m210-ProductAuthorizationRequest.tyk.json`
- `m095-Inbound.m096.json`

При `mvn package` схемы копируются в classpath. Маппинг — schema-guided projection: при добавлении атрибутов в схему они автоматически попадут в результат, если присутствуют в JSON теста в БД. Ручная доработка маппера не нужна.

---

## Типовой сценарий

1. `mvn -DskipTests package && docker compose up --build -d`
2. Swagger → **Запустить регресс** (все тесты или, например, `CASH`).
3. Скопировать `runId`.
4. Подождать несколько секунд.
5. Открыть `GET /report/{runId}`.
6. При необходимости — логи пода (`docker compose logs tester3000`) и SQL:

```bash
docker compose exec -T postgres psql -U tester -d tester3000 \
  -c "select test_name, step, type, status from process where run_id = '<runId>' order by created_at;"
```

---

## Структура репозитория

```text
tester3000/
├── contract/schemas/         # JSON Schema m210 / m095 (источник истины)
├── src/main/java/ru/vtb/tester3000/
│   ├── controller/           # REST + Swagger
│   ├── service/              # регресс, dispatch, отчёт, каталог тестов
│   ├── kafka/                # publishers, consumers, stub
│   ├── mapper/               # schema-guided маппинг
│   ├── entity/ repository/
│   └── config/
├── src/main/resources/
│   ├── application.yml
│   └── db/changelog/         # Liquibase + seed тестов
├── docker-compose.yml
├── Dockerfile
└── README.md
```

---

## Заметки

- HTML-отчёт — view над БД, не файл.
- Default `tests` в Swagger обновляется при рестарте.
- После `code: 0` фон ещё может работать; для полного отчёта дождитесь outbox.
