package ru.vtb.tester3000.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * Schema-guided projection: оставляет во входном JSON только то, что разрешено схемой.
 * Расширение схемы новыми атрибутами подхватывается автоматически — если поле есть
 * в сообщении из БД и объявлено в схеме, оно попадёт в результат без правок кода.
 */
@Component
public class SchemaPruner {

    private final ObjectMapper objectMapper;

    public SchemaPruner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode prune(JsonNode document, JsonNode schemaRoot) {
        return pruneNode(document, schemaRoot, schemaRoot);
    }

    private JsonNode pruneNode(JsonNode value, JsonNode schema, JsonNode schemaRoot) {
        if (value == null || value.isNull() || schema == null || schema.isMissingNode()) {
            return value;
        }
        schema = normalize(schema, schemaRoot);

        if (schema.has("oneOf") || schema.has("anyOf")) {
            JsonNode alternatives = schema.has("oneOf") ? schema.get("oneOf") : schema.get("anyOf");
            JsonNode best = null;
            int bestScore = -1;
            for (JsonNode alt : alternatives) {
                JsonNode projected = pruneNode(value, alt, schemaRoot);
                int score = countLeaves(projected);
                if (score > bestScore) {
                    bestScore = score;
                    best = projected;
                }
            }
            return best != null ? best : value.deepCopy();
        }

        String type = schema.path("type").asText(null);
        if (type == null && schema.has("properties")) {
            type = "object";
        }

        if ("object".equals(type) || (type == null && value.isObject() && schema.has("properties"))) {
            if (!value.isObject()) {
                return value;
            }
            return projectObject((ObjectNode) value, schema, schemaRoot);
        }

        // object без properties (пример: m095.cbiRequest) — произвольное дерево
        if ("object".equals(type) && value.isObject()) {
            return value.deepCopy();
        }

        if ("array".equals(type) || (type == null && value.isArray())) {
            if (!value.isArray()) {
                return value;
            }
            ArrayNode result = objectMapper.createArrayNode();
            JsonNode itemSchema = schema.path("items");
            for (JsonNode item : value) {
                result.add(pruneNode(item, itemSchema, schemaRoot));
            }
            return result;
        }

        return value.deepCopy();
    }

    private ObjectNode projectObject(ObjectNode source, JsonNode schema, JsonNode schemaRoot) {
        ObjectNode result = objectMapper.createObjectNode();
        JsonNode properties = schema.path("properties");
        boolean additionalAllowed = isAdditionalAllowed(schema);

        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode child = entry.getValue();
            if (properties.isObject() && properties.has(name)) {
                result.set(name, pruneNode(child, properties.get(name), schemaRoot));
            } else if (additionalAllowed) {
                if (schema.path("additionalProperties").isObject()) {
                    result.set(name, pruneNode(child, schema.get("additionalProperties"), schemaRoot));
                } else {
                    result.set(name, child.deepCopy());
                }
            }
        }
        applyEnumFixes(result, properties);
        return result;
    }

    /**
     * Разыменовывает $ref и схлопывает allOf в одну схему с объединёнными properties.
     */
    private JsonNode normalize(JsonNode schema, JsonNode schemaRoot) {
        schema = resolveRef(schema, schemaRoot);
        if (schema.has("allOf")) {
            return mergeAllOf(schema.get("allOf"), schemaRoot);
        }
        return schema;
    }

    private JsonNode resolveRef(JsonNode schema, JsonNode schemaRoot) {
        int guard = 0;
        while (schema != null && schema.has("$ref") && guard++ < 32) {
            String ref = schema.get("$ref").asText();
            if (!ref.startsWith("#/")) {
                throw new IllegalArgumentException("Unsupported $ref: " + ref);
            }
            JsonNode current = schemaRoot;
            for (String part : ref.substring(2).split("/")) {
                current = current.path(part.replace("~1", "/").replace("~0", "~"));
            }
            if (current.isMissingNode()) {
                throw new IllegalArgumentException("Unresolved $ref: " + ref);
            }
            schema = current;
        }
        return schema;
    }

    private JsonNode mergeAllOf(JsonNode allOf, JsonNode schemaRoot) {
        ObjectNode merged = objectMapper.createObjectNode();
        merged.put("type", "object");
        ObjectNode mergedProps = merged.putObject("properties");
        boolean additional = true;

        for (JsonNode part : allOf) {
            JsonNode resolved = normalize(part, schemaRoot);
            if (resolved.has("properties") && resolved.get("properties").isObject()) {
                resolved.get("properties").fields()
                        .forEachRemaining(e -> mergedProps.set(e.getKey(), e.getValue()));
            }
            if (!isAdditionalAllowed(resolved)) {
                additional = false;
            }
            if (resolved.has("required") && resolved.get("required").isArray()) {
                ArrayNode required = merged.has("required")
                        ? (ArrayNode) merged.get("required")
                        : merged.putArray("required");
                for (JsonNode req : resolved.get("required")) {
                    required.add(req.deepCopy());
                }
            }
        }
        merged.put("additionalProperties", additional);
        return merged;
    }

    private static boolean isAdditionalAllowed(JsonNode schema) {
        JsonNode additional = schema.get("additionalProperties");
        if (additional == null || additional.isMissingNode() || additional.isNull()) {
            return true;
        }
        if (additional.isBoolean()) {
            return additional.booleanValue();
        }
        return additional.isObject();
    }

    private void applyEnumFixes(ObjectNode object, JsonNode properties) {
        if (properties == null || !properties.isObject()) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> it = properties.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String name = entry.getKey();
            JsonNode propSchema = entry.getValue();
            if (!object.has(name) || !object.get(name).isTextual() || !propSchema.has("enum")) {
                continue;
            }
            String actual = object.get(name).asText();
            for (JsonNode allowed : propSchema.get("enum")) {
                if (allowed.isTextual() && allowed.asText().equalsIgnoreCase(actual)) {
                    object.put(name, allowed.asText());
                    break;
                }
            }
        }
    }

    private int countLeaves(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return 0;
        }
        if (node.isValueNode()) {
            return 1;
        }
        int count = 0;
        if (node.isObject()) {
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                count += countLeaves(it.next());
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                count += countLeaves(item);
            }
        }
        return count;
    }
}
