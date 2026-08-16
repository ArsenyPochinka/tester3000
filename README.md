# tester3000

Сквозной регресс авторизаций: формирует сообщения по выбранным тест-кейсам из БД, отправляет их во внешние контуры (m210 HTTP + Kafka clearing), слушает outbox-события и фиксирует шаги в PostgreSQL. Управление — через Swagger API.

---

## Что делает приложение

1. Принимает параметры карты и выбор кейсов (`tests` и/или `regressionTag`).
2. Берёт исходные JSON из таблицы `regression_cases` (до 3 пар auth/clr на кейс).
3. Маппит их на контракты (вход — сырой m025 **или** уже m210/m095):
   - авторизация → **m210** (`ProductAuthorizationRequest`);
   - клиринг → **m095** (`Inbound`).
4. Параметризует `card`, генерирует `rId`, общий `match.key` / `link.key` в паре, обновляет даты (кроме `expDate`).
5. Сохраняет подготовленные primary-сообщения в `regression_test_message`.
6. Сразу возвращает `runId` и код запуска (`0` = принят).
7. В фоне кейсы стартуют с лимитом `parallel-tests` (по умолчанию 2 сразу, остальные через `test-start-interval-ms`).
   Внутри кейса — слоты `auth → clr → auth_add_* → clr_add_*` с паузой `message-delay-ms`;
   при ошибке auth остальные сообщения кейса не отправляются; после успешной отправки — stub outbox (если включён).
8. Слушает три outbox-топика и пишет события в `process`.
9. По `runId` отдаёт HTML-отчёт.

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

Или только по тегу:

```json
{
  "card": { "...": "..." },
  "regressionTag": "Pochinka"
}
```

- `card` — параметры карты для параметризации сообщений.
- `tests` — коды из `regression_cases`. В Swagger example/enum подставляются все коды на момент старта.
- `regressionTag` — тег регресса. В Swagger доступны все теги из БД (enum + example первого тега). Можно указать вместо `tests` или вместе (тогда пересечение).

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

Неизвестный код / неизвестный тег / нет ни `tests`, ни `regressionTag` / невалидная карта → `400`.

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
    ├─ выбор кейсов (tests и/или regressionTag)
    ├─ маппинг слотов → m210 / m095 + card
    ├─ запись primary в regression_test_message
    ├─ ответ { runId, code: 0 }
    │
    └─ фон:
           первые parallel-tests кейсов сразу, остальные через test-start-interval-ms
           внутри кейса: AUTH/CLR по слотам, pause message-delay-ms
           ошибка AUTH → остальные сообщения кейса пропускаются
           stub outbox (если вкл.) / consumers → process
```

### Связка сообщений

- У каждого auth/clearing свой `rId` (`reqId` в process).
- Внутри слота (primary / add_1 / add_2) общий ключ: `match.key` (auth) = `link[].key` (clearing) — 40 символов `[A-Z0-9]` + 18-значный RRN.
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

Таблица `regression_cases` — единственный источник кейсов.

| Колонка | Тип | Смысл |
|---------|-----|--------|
| `test_code` | varchar PK | Код теста |
| `test_description` | text | Описание |
| `regression_tag` | varchar | Тег регресса (запуск по тегу) |
| `auth` / `clr` | jsonb | Primary auth/clearing (любое может быть NULL) |
| `auth_add_1` / `clr_add_1` | jsonb | Доп. пара 1 |
| `auth_add_2` / `clr_add_2` | jsonb | Доп. пара 2 |

В полях сообщений допускается сырой JSON как в m025 **или** уже готовое сообщение по схемам m210 / m095 — параметризация одинаковая.

| test_code | Описание | regression_tag |
|-----------|----------|----------------|
| `CASH` | Снятие наличных | `Pochinka` |
| `DEPOSIT` | Внесение наличных | `Pochinka` |
| `GOODS` | Покупка товаров | `Pochinka` |
| `GOODS_WITH_CASHBACK` | Покупка товаров с кэшбэком | `Pochinka` |
| `PAYMENT_DEBIT` | Дебетовый платёж | `Pochinka` |
| `PAYMENT_CREDIT` | Кредитовый платёж | `Pochinka` |

Новый тест — insert в таблицу (или Liquibase seed) и рестарт приложения, если нужен обновлённый default-список кодов/тегов в Swagger.

---

## Таблицы результатов

### `regression_test_message`

Подготовленные primary-сообщения прогона: `id`, `run_id`, `test_name`, `auth_message`, `clearing_message`, `created_at`.
Доп. слоты (`auth_add_*` / `clr_add_*`) видны в `process.result` и логах.

### `process`

Журнал шагов. Связь: `run_id` + `test_message_id` → `regression_test_message`.

---

## Конфигурация

| Параметр | Env | Default | Назначение |
|----------|-----|---------|------------|
| `tester3000.message-delay-ms` | `MESSAGE_DELAY_MS` | `2000` | Пауза между сообщениями внутри одного кейса |
| `tester3000.parallel-tests` | `PARALLEL_TESTS` | `2` | Сколько кейсов выполняется одновременно |
| `tester3000.test-start-interval-ms` | `TEST_START_INTERVAL_MS` | `3000` | Интервал старта кейсов после первых `parallel-tests` |
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
- Defaults в Swagger (коды/теги) и пул `parallel-tests` берутся на старте — после смены конфига нужен рестарт.
- После `code: 0` фон ещё может работать; для полного отчёта дождитесь outbox.
