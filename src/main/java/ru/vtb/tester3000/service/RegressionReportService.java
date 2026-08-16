package ru.vtb.tester3000.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.vtb.tester3000.entity.ProcessEntity;
import ru.vtb.tester3000.entity.RegressionTestMessageEntity;
import ru.vtb.tester3000.repository.ProcessRepository;
import ru.vtb.tester3000.repository.RegressionTestMessageRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RegressionReportService {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final RegressionTestMessageRepository messageRepository;
    private final ProcessRepository processRepository;
    private final ObjectMapper objectMapper;

    public RegressionReportService(
            RegressionTestMessageRepository messageRepository,
            ProcessRepository processRepository,
            ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.processRepository = processRepository;
        this.objectMapper = objectMapper;
    }

    public String buildHtmlReport(UUID runId) {
        List<RegressionTestMessageEntity> messages = messageRepository.findByRunIdOrderByTestNameAsc(runId);
        List<ProcessEntity> processes = processRepository.findByRunIdOrderByCreatedAtAsc(runId);
        if (messages.isEmpty() && processes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Прогон не найден: " + runId);
        }

        Map<String, List<ProcessEntity>> byTest = processes.stream()
                .collect(Collectors.groupingBy(ProcessEntity::getTestName, LinkedHashMap::new, Collectors.toList()));

        long successSend = processes.stream()
                .filter(p -> p.getStep() == ProcessEntity.Step.AUTH_SEND || p.getStep() == ProcessEntity.Step.CLEARING_SEND)
                .filter(p -> p.getStatus() == ProcessEntity.Status.SUCCESS)
                .count();
        long errorSend = processes.stream()
                .filter(p -> p.getStep() == ProcessEntity.Step.AUTH_SEND || p.getStep() == ProcessEntity.Step.CLEARING_SEND)
                .filter(p -> p.getStatus() == ProcessEntity.Status.ERROR)
                .count();
        long outboxEvents = processes.stream()
                .filter(p -> p.getStep() == ProcessEntity.Step.FIN_MESSAGE_25
                        || p.getStep() == ProcessEntity.Step.FIN_INSTRUCTION_104
                        || p.getStep() == ProcessEntity.Step.FIN_TRANSACTION_39)
                .count();

        InstantBounds bounds = bounds(messages, processes);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        html.append("<title>Отчёт регресса ").append(esc(runId.toString())).append("</title>");
        html.append("<style>");
        html.append(css());
        html.append("</style></head><body>");
        html.append("<header class=\"hero\">");
        html.append("<p class=\"brand\">tester3000</p>");
        html.append("<h1>Отчёт по прогону</h1>");
        html.append("<p class=\"meta\"><span>run_id</span> <code>").append(esc(runId.toString())).append("</code></p>");
        if (bounds.start != null) {
            html.append("<p class=\"meta\"><span>начало</span> ").append(esc(TIME_FMT.format(bounds.start))).append("</p>");
        }
        if (bounds.end != null) {
            html.append("<p class=\"meta\"><span>последнее событие</span> ").append(esc(TIME_FMT.format(bounds.end))).append("</p>");
        }
        html.append("<div class=\"stats\">");
        html.append(stat("Тестов", String.valueOf(messages.size())));
        html.append(stat("Успешных отправок", String.valueOf(successSend)));
        html.append(stat("Ошибок отправки", String.valueOf(errorSend)));
        html.append(stat("Событий outbox", String.valueOf(outboxEvents)));
        html.append("</div></header>");

        html.append("<main>");
        List<String> testOrder = new ArrayList<>();
        for (RegressionTestMessageEntity msg : messages) {
            if (!testOrder.contains(msg.getTestName())) {
                testOrder.add(msg.getTestName());
            }
        }
        for (String name : byTest.keySet()) {
            if (!testOrder.contains(name)) {
                testOrder.add(name);
            }
        }

        for (String testName : testOrder) {
            RegressionTestMessageEntity msg = messages.stream()
                    .filter(m -> testName.equals(m.getTestName()))
                    .findFirst()
                    .orElse(null);
            List<ProcessEntity> steps = byTest.getOrDefault(testName, List.of()).stream()
                    .sorted(Comparator.comparing(ProcessEntity::getCreatedAt))
                    .toList();

            html.append("<section class=\"test\">");
            html.append("<h2>").append(esc(testName)).append("</h2>");
            if (msg != null) {
                html.append("<p class=\"sub\">message_id: <code>").append(esc(msg.getId().toString())).append("</code>");
                html.append(" · создан: ").append(esc(TIME_FMT.format(msg.getCreatedAt()))).append("</p>");
                html.append("<div class=\"payloads\">");
                html.append(payloadBlock("Auth", msg.getAuthMessage()));
                if (msg.getClearingMessage() != null && !msg.getClearingMessage().isBlank()) {
                    html.append(payloadBlock("Clearing", msg.getClearingMessage()));
                } else {
                    html.append("<p class=\"muted\">Клиринг отсутствует</p>");
                }
                html.append("</div>");
            }

            html.append("<table><thead><tr>");
            html.append("<th>Время</th><th>Шаг</th><th>Type</th><th>Status</th><th>reqId</th><th>Описание</th><th>Result</th>");
            html.append("</tr></thead><tbody>");
            if (steps.isEmpty()) {
                html.append("<tr><td colspan=\"7\" class=\"muted\">Нет записей process</td></tr>");
            } else {
                for (ProcessEntity p : steps) {
                    html.append("<tr class=\"").append(statusClass(p.getStatus())).append("\">");
                    html.append("<td>").append(esc(TIME_FMT.format(p.getCreatedAt()))).append("</td>");
                    html.append("<td><code>").append(esc(p.getStep().getDbValue())).append("</code></td>");
                    html.append("<td>").append(esc(p.getType().name())).append("</td>");
                    html.append("<td><span class=\"badge\">").append(esc(p.getStatus().name())).append("</span></td>");
                    html.append("<td><code class=\"rid\">").append(esc(p.getReqId())).append("</code></td>");
                    html.append("<td>").append(esc(p.getDescription())).append("</td>");
                    html.append("<td>");
                    if (p.getResult() != null && !p.getResult().isBlank()) {
                        html.append("<details><summary>показать</summary><pre>")
                                .append(esc(prettyMaybe(p.getResult())))
                                .append("</pre></details>");
                    } else {
                        html.append("<span class=\"muted\">—</span>");
                    }
                    html.append("</td></tr>");
                }
            }
            html.append("</tbody></table></section>");
        }

        html.append("</main>");
        html.append("<footer>tester3000 · HTML-отчёт по run_id</footer>");
        html.append("</body></html>");
        return html.toString();
    }

    private String payloadBlock(String title, String json) {
        return "<details class=\"payload\"><summary>" + esc(title) + "</summary><pre>"
                + esc(prettyMaybe(json)) + "</pre></details>";
    }

    private String prettyMaybe(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return raw;
        }
        try {
            Object tree = objectMapper.readValue(trimmed, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        } catch (Exception e) {
            return raw;
        }
    }

    private static String stat(String label, String value) {
        return "<div class=\"stat\"><strong>" + esc(value) + "</strong><span>" + esc(label) + "</span></div>";
    }

    private static String statusClass(ProcessEntity.Status status) {
        return switch (status) {
            case SUCCESS, Approved, Executed, Completed, Accepted -> "ok";
            case ERROR, Error, Rejected, SystemError, Canceled -> "bad";
            default -> "neutral";
        };
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String css() {
        return """
                :root {
                  --bg: #f3efe6;
                  --ink: #1c1915;
                  --muted: #6b645a;
                  --line: #d9d0c2;
                  --card: #fffdf8;
                  --ok: #1f6b4a;
                  --bad: #8b2e2e;
                  --accent: #0f4c5c;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  font-family: "IBM Plex Sans", "Segoe UI", sans-serif;
                  color: var(--ink);
                  background:
                    radial-gradient(circle at 10% 0%, #efe6d4 0%, transparent 45%),
                    radial-gradient(circle at 90% 10%, #d9e6e8 0%, transparent 40%),
                    var(--bg);
                }
                .hero, main, footer { max-width: 1200px; margin: 0 auto; padding: 24px; }
                .brand {
                  margin: 0 0 8px;
                  font-size: 14px;
                  letter-spacing: 0.08em;
                  text-transform: uppercase;
                  color: var(--accent);
                  font-weight: 700;
                }
                h1 { margin: 0 0 12px; font-size: 32px; line-height: 1.15; }
                h2 { margin: 0 0 8px; font-size: 22px; }
                .meta { margin: 4px 0; color: var(--muted); }
                .meta span { display: inline-block; min-width: 140px; color: var(--ink); font-weight: 600; }
                code, pre { font-family: "IBM Plex Mono", Consolas, monospace; }
                .stats { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 20px; }
                .stat {
                  background: var(--card);
                  border: 1px solid var(--line);
                  padding: 12px 16px;
                  min-width: 140px;
                }
                .stat strong { display: block; font-size: 24px; }
                .stat span { color: var(--muted); font-size: 13px; }
                .test {
                  background: var(--card);
                  border: 1px solid var(--line);
                  padding: 18px;
                  margin-bottom: 18px;
                }
                .sub { color: var(--muted); margin: 0 0 12px; }
                .payloads { display: grid; gap: 8px; margin-bottom: 14px; }
                details.payload, details { margin: 0; }
                summary { cursor: pointer; font-weight: 600; color: var(--accent); }
                pre {
                  white-space: pre-wrap;
                  word-break: break-word;
                  background: #f7f2e8;
                  border: 1px solid var(--line);
                  padding: 10px;
                  max-height: 320px;
                  overflow: auto;
                  font-size: 12px;
                }
                table { width: 100%; border-collapse: collapse; font-size: 13px; }
                th, td { border-bottom: 1px solid var(--line); padding: 8px 6px; vertical-align: top; text-align: left; }
                th { font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; color: var(--muted); }
                .badge {
                  display: inline-block;
                  padding: 2px 8px;
                  border: 1px solid var(--line);
                  background: #fff;
                  font-weight: 600;
                }
                tr.ok .badge { color: var(--ok); border-color: #b7d7c7; background: #eef8f2; }
                tr.bad .badge { color: var(--bad); border-color: #e0b6b6; background: #fbeeee; }
                .rid { font-size: 11px; }
                .muted { color: var(--muted); }
                footer { color: var(--muted); font-size: 13px; padding-bottom: 40px; }
                @media (max-width: 800px) {
                  table { display: block; overflow-x: auto; }
                }
                """;
    }

    private record InstantBounds(java.time.Instant start, java.time.Instant end) {
    }

    private static InstantBounds bounds(
            List<RegressionTestMessageEntity> messages,
            List<ProcessEntity> processes
    ) {
        java.time.Instant start = null;
        java.time.Instant end = null;
        for (RegressionTestMessageEntity m : messages) {
            if (start == null || m.getCreatedAt().isBefore(start)) {
                start = m.getCreatedAt();
            }
            if (end == null || m.getCreatedAt().isAfter(end)) {
                end = m.getCreatedAt();
            }
        }
        for (ProcessEntity p : processes) {
            if (start == null || p.getCreatedAt().isBefore(start)) {
                start = p.getCreatedAt();
            }
            if (end == null || p.getCreatedAt().isAfter(end)) {
                end = p.getCreatedAt();
            }
        }
        return new InstantBounds(start, end);
    }
}
