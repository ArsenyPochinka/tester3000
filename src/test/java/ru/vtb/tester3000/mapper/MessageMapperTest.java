package ru.vtb.tester3000.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.vtb.tester3000.dto.CardAuth;
import ru.vtb.tester3000.dto.CardEmv;
import ru.vtb.tester3000.dto.CardParam;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MessageMapper mapper;
    private LinkageKeyGenerator.LinkageKey linkage;

    @BeforeEach
    void setUp() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T12:00:00Z"), ZoneOffset.UTC);
        linkage = new LinkageKeyGenerator.LinkageKey(
                "C2C79672014882477C66D2192ABBB755DDB60FE7260802390625398999",
                "260802390625398999"
        );
        mapper = new MessageMapper(
                objectMapper,
                new SchemaPruner(objectMapper),
                clock,
                new ClassPathResource("schemas/m210-ProductAuthorizationRequest.tyk.json"),
                new ClassPathResource("schemas/m095-Inbound.m096.json")
        );
    }

    @Test
    void mapsGoodsAuthToValidM210() throws Exception {
        JsonNode raw = readFixture("fixtures/GOODS/auth.json");
        CardParam card = sampleCard();

        JsonNode mapped = mapper.mapAuthorization(raw, card, linkage);

        assertFalse(mapped.has("lmdAttrs"));
        assertFalse(mapped.has("status"));
        assertFalse(mapped.path("cbiRequest").has("holdActions"));
        assertEquals("Sync", mapped.at("/cbiRequest/tranRequest/userAttrs/mode").asText());
        assertEquals(card.getCardId(), mapped.at("/cbiRequest/tranRequest/parties/cust/token/0/card/cardId").asText());
        assertEquals(card.getPlasticId(), mapped.at("/cbiRequest/tranRequest/parties/cust/token/0/card/plasticId").asText());
        assertEquals(card.getExpDate(), mapped.at("/cbiRequest/tranRequest/parties/cust/token/0/card/expDate").asText());
        assertEquals("2026-08-14T12:00:00.000", mapped.at("/cbiRequest/tranRequest/localTime").asText());
        assertEquals(linkage.key(), mapped.at("/cbiRequest/tranRequest/match/key").asText());
        assertEquals(linkage.rrn(), mapped.at("/cbiRequest/tranRequest/match/rrn").asText());

        Set<ValidationMessage> errors = validate(mapped, mapper.authSchema());
        assertTrue(errors.isEmpty(), () -> "Schema errors: " + errors);
    }

    @Test
    void mapsGoodsClearingToValidM095() throws Exception {
        JsonNode raw = readFixture("fixtures/GOODS/clr.json");
        CardParam card = sampleCard();

        JsonNode mapped = mapper.mapClearing(raw, card, linkage);

        assertTrue(mapped.has("rId"));
        assertTrue(mapped.has("cbiRequest"));
        assertEquals(card.getCardId(), mapped.at("/cbiRequest/tranRequest/parties/cust/token/0/card/cardId").asText());
        assertEquals(linkage.key(), mapped.at("/cbiRequest/tranRequest/link/0/key").asText());

        Set<ValidationMessage> errors = validate(mapped, mapper.clearingSchema());
        assertTrue(errors.isEmpty(), () -> "Schema errors: " + errors);
    }

    @Test
    void authMatchKeyEqualsClearingLinkKey() throws Exception {
        CardParam card = sampleCard();

        JsonNode auth = mapper.mapAuthorization(readFixture("fixtures/GOODS/auth.json"), card, linkage);
        JsonNode clr = mapper.mapClearing(readFixture("fixtures/GOODS/clr.json"), card, linkage);

        assertEquals(
                auth.at("/cbiRequest/tranRequest/match/key").asText(),
                clr.at("/cbiRequest/tranRequest/link/0/key").asText()
        );
        assertTrue(auth.at("/cbiRequest/tranRequest/match/key").asText().endsWith(linkage.rrn()));
        assertEquals(58, auth.at("/cbiRequest/tranRequest/match/key").asText().length());
    }

    private JsonNode readFixture(String classpath) throws Exception {
        try (InputStream in = new ClassPathResource(classpath).getInputStream()) {
            return objectMapper.readTree(in);
        }
    }

    private Set<ValidationMessage> validate(JsonNode document, JsonNode schemaNode) {
        var normalized = schemaNode.deepCopy();
        if (normalized.isObject() && normalized.has("$id")) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) normalized)
                    .put("$id", "https://tester3000.local/schemas/" + normalized.get("$id").asText() + ".json");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema schema = factory.getSchema(normalized);
        return schema.validate(document);
    }

    private CardParam sampleCard() {
        CardParam card = new CardParam();
        CardAuth auth = new CardAuth();
        auth.setPresence(true);
        card.setAuth(auth);
        card.setPlasticId("11111111-1111-1111-1111-111111111111");
        card.setCardId("22222222-2222-2222-2222-222222222222");
        card.setExpDate("2027-01-01T00:00:00.000");
        CardEmv emv = new CardEmv();
        emv.setMbr(1L);
        card.setEmv(emv);
        return card;
    }
}
