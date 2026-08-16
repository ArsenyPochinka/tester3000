package ru.vtb.tester3000.controller;

import io.swagger.v3.oas.annotations.Hidden;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Локальная заглушка m210 (продуктовая авторизация).
 * Включается через tester3000.m210.stub-enabled=true.
 */
@Hidden
@RestController
@RequestMapping(path = "/api/1.1.0/prod_auth", produces = MediaType.APPLICATION_JSON_VALUE)
@ConditionalOnProperty(prefix = "tester3000.m210", name = "stub-enabled", havingValue = "true", matchIfMissing = true)
public class ProductAuthorizationStubController {

    private static final Logger log = LoggerFactory.getLogger(ProductAuthorizationStubController.class);

    @PostMapping(path = "/_request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode authorize(
            @RequestHeader(value = "x-mdm-id", required = false) String mdmId,
            @RequestBody JsonNode request
    ) {
        String rId = request.path("rId").asText("");
        log.info("m210 stub accepted auth rId={} x-mdm-id={}", rId, mdmId);

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("rId", rId);
        payload.put("transactionResult", "Approved");
        payload.put("approvalCode", "STUB01");

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("operationStatus", "OK");
        response.set("operationPayload", payload);
        return response;
    }
}
