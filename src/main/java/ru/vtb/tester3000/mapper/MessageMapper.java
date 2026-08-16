package ru.vtb.tester3000.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.vtb.tester3000.dto.CardParam;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Маппинг сырых сообщений из БД на контракты m210 / m095.
 * Форма результата определяется JSON Schema из {@code contract/schemas}
 * (schema-guided projection): новые атрибуты схемы подхватываются автоматически,
 * если они присутствуют во входном сообщении.
 */
@Component
public class MessageMapper {

    private static final DateTimeFormatter LOCAL_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final Pattern LOCAL_DT =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}$");
    private static final Pattern OFFSET_DT =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$");

    private final ObjectMapper objectMapper;
    private final SchemaPruner schemaPruner;
    private final Clock clock;
    private final JsonNode authSchema;
    private final JsonNode clearingSchema;

    public MessageMapper(
            ObjectMapper objectMapper,
            SchemaPruner schemaPruner,
            Clock clock,
            @Value("classpath:schemas/m210-ProductAuthorizationRequest.tyk.json") Resource authSchemaResource,
            @Value("classpath:schemas/m095-Inbound.m096.json") Resource clearingSchemaResource
    ) throws IOException {
        this.objectMapper = objectMapper;
        this.schemaPruner = schemaPruner;
        this.clock = clock;
        try (InputStream authIn = authSchemaResource.getInputStream();
             InputStream clrIn = clearingSchemaResource.getInputStream()) {
            this.authSchema = objectMapper.readTree(authIn);
            this.clearingSchema = objectMapper.readTree(clrIn);
        }
    }

    public JsonNode mapAuthorization(JsonNode raw, CardParam card, LinkageKeyGenerator.LinkageKey linkage) {
        JsonNode pruned = schemaPruner.prune(raw, authSchema);
        ObjectNode result = (ObjectNode) pruned.deepCopy();
        result.put("rId", UUID.randomUUID().toString());
        applyCard(result, card, true);
        applyAuthLinkage(result, linkage);
        refreshDates(result, "expDate");
        return result;
    }

    public JsonNode mapClearing(JsonNode raw, CardParam card, LinkageKeyGenerator.LinkageKey linkage) {
        JsonNode pruned = schemaPruner.prune(raw, clearingSchema);
        ObjectNode result = (ObjectNode) pruned.deepCopy();
        result.put("rId", UUID.randomUUID().toString());
        applyCard(result, card, false);
        applyClearingLinkage(result, linkage);
        refreshDates(result, "expDate");
        return result;
    }

    private void applyAuthLinkage(ObjectNode root, LinkageKeyGenerator.LinkageKey linkage) {
        ObjectNode tran = objectAt(root, "cbiRequest", "tranRequest");
        if (tran == null) {
            return;
        }
        ObjectNode match = tran.has("match") && tran.get("match").isObject()
                ? (ObjectNode) tran.get("match")
                : tran.putObject("match");
        match.put("key", linkage.key());
        match.put("rrn", linkage.rrn());
    }

    private void applyClearingLinkage(ObjectNode root, LinkageKeyGenerator.LinkageKey linkage) {
        ObjectNode tran = objectAt(root, "cbiRequest", "tranRequest");
        if (tran == null) {
            return;
        }
        ArrayNode link;
        if (tran.has("link") && tran.get("link").isArray()) {
            link = (ArrayNode) tran.get("link");
        } else {
            link = tran.putArray("link");
        }
        if (link.isEmpty()) {
            ObjectNode item = link.addObject();
            item.put("key", linkage.key());
        } else {
            for (JsonNode itemNode : link) {
                if (itemNode.isObject()) {
                    ((ObjectNode) itemNode).put("key", linkage.key());
                }
            }
        }
    }

    private ObjectNode objectAt(ObjectNode root, String... path) {
        JsonNode current = root;
        for (String part : path) {
            if (current == null || !current.isObject() || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        return current != null && current.isObject() ? (ObjectNode) current : null;
    }

    private void applyCard(ObjectNode root, CardParam card, boolean fullCard) {
        JsonNode tokens = root.at("/cbiRequest/tranRequest/parties/cust/token");
        if (!tokens.isArray()) {
            return;
        }
        for (JsonNode tokenNode : tokens) {
            if (!tokenNode.isObject()) {
                continue;
            }
            ObjectNode token = (ObjectNode) tokenNode;
            ObjectNode cardNode = token.has("card") && token.get("card").isObject()
                    ? (ObjectNode) token.get("card")
                    : token.putObject("card");

            cardNode.put("cardId", card.getCardId());
            if (fullCard) {
                if (card.getPlasticId() != null) {
                    cardNode.put("plasticId", card.getPlasticId());
                }
                if (card.getExpDate() != null) {
                    cardNode.put("expDate", card.getExpDate());
                }
                if (card.getAuth() != null) {
                    ObjectNode auth = cardNode.putObject("auth");
                    if (card.getAuth().getPresence() != null) {
                        auth.put("presence", card.getAuth().getPresence());
                    }
                    if (card.getAuth().getPinChecked() != null) {
                        auth.put("pinChecked", card.getAuth().getPinChecked());
                    }
                }
                if (card.getEmv() != null && card.getEmv().getMbr() != null) {
                    ObjectNode emv = cardNode.putObject("emv");
                    emv.put("mbr", card.getEmv().getMbr());
                }
            }
            if (card.getEntryMode() != null) {
                cardNode.put("entryMode", card.getEntryMode());
            }
        }
    }

    private void refreshDates(JsonNode node, String excludedField) {
        LocalDateTime nowLocal = LocalDateTime.now(clock);
        OffsetDateTime nowOffset = OffsetDateTime.now(clock);
        String localValue = LOCAL_MILLIS.format(nowLocal);
        refreshDatesRecursive(node, excludedField, localValue, nowOffset);
    }

    private void refreshDatesRecursive(
            JsonNode node,
            String excludedField,
            String localValue,
            OffsetDateTime nowOffset
    ) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String name = entry.getKey();
                JsonNode value = entry.getValue();
                if (excludedField.equals(name)) {
                    continue;
                }
                if (value.isTextual()) {
                    String text = value.asText();
                    if (LOCAL_DT.matcher(text).matches()) {
                        object.put(name, localValue);
                    } else if (OFFSET_DT.matcher(text).matches()) {
                        object.put(name, nowOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    }
                } else {
                    refreshDatesRecursive(value, excludedField, localValue, nowOffset);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                refreshDatesRecursive(item, excludedField, localValue, nowOffset);
            }
        }
    }

    JsonNode authSchema() {
        return authSchema;
    }

    JsonNode clearingSchema() {
        return clearingSchema;
    }
}
