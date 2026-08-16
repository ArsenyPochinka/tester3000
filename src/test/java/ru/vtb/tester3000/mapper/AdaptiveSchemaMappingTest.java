package ru.vtb.tester3000.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.vtb.tester3000.dto.CardAuth;
import ru.vtb.tester3000.dto.CardEmv;
import ru.vtb.tester3000.dto.CardParam;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Новые атрибуты схемы подхватываются автоматически, если есть во входном JSON.
 */
class AdaptiveSchemaMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private SchemaPruner pruner;
    private JsonNode baseAuthSchema;
    private MessageMapper mapper;
    private LinkageKeyGenerator.LinkageKey linkage;

    @BeforeEach
    void setUp() throws Exception {
        pruner = new SchemaPruner(objectMapper);
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T12:00:00Z"), ZoneOffset.UTC);
        linkage = new LinkageKeyGenerator.LinkageKey(
                "C2C79672014882477C66D2192ABBB755DDB60FE7260802390625398999",
                "260802390625398999"
        );
        try (var in = new ClassPathResource("schemas/m210-ProductAuthorizationRequest.tyk.json").getInputStream()) {
            baseAuthSchema = objectMapper.readTree(in);
        }
        mapper = new MessageMapper(
                objectMapper,
                pruner,
                clock,
                new ClassPathResource("schemas/m210-ProductAuthorizationRequest.tyk.json"),
                new ClassPathResource("schemas/m095-Inbound.m096.json")
        );
    }

    @Test
    void keepsNewlyDeclaredSchemaFieldWhenPresentInSource() throws Exception {
        JsonNode extendedSchema = extendCardTokenWith(baseAuthSchema, "loyaltyId", "string");

        ObjectNode raw = (ObjectNode) objectMapper.readTree(
                new ClassPathResource("fixtures/GOODS/auth.json").getInputStream());
        ObjectNode card = (ObjectNode) raw.at("/cbiRequest/tranRequest/parties/cust/token/0/card");
        card.put("loyaltyId", "LOYAL-42");
        card.put("obsoleteNoise", "should-be-dropped");

        JsonNode pruned = pruner.prune(raw, extendedSchema);

        assertEquals("LOYAL-42", pruned.at("/cbiRequest/tranRequest/parties/cust/token/0/card/loyaltyId").asText());
        assertFalse(pruned.at("/cbiRequest/tranRequest/parties/cust/token/0/card").has("obsoleteNoise"));
        assertTrue(pruned.at("/cbiRequest/tranRequest/parties/cust/token/0/card").has("cardId"));
    }

    @Test
    void omitsNewSchemaFieldWhenAbsentInSource() throws Exception {
        JsonNode extendedSchema = extendCardTokenWith(baseAuthSchema, "loyaltyId", "string");

        ObjectNode raw = (ObjectNode) objectMapper.readTree(
                new ClassPathResource("fixtures/GOODS/auth.json").getInputStream());

        JsonNode pruned = pruner.prune(raw, extendedSchema);
        assertTrue(pruned.at("/cbiRequest/tranRequest/parties/cust/token/0/card/loyaltyId").isMissingNode());
    }

    @Test
    void clearingKeepsArbitraryCbiRequestExtensions() throws Exception {
        ObjectNode raw = (ObjectNode) objectMapper.readTree(
                new ClassPathResource("fixtures/GOODS/clr.json").getInputStream());
        ((ObjectNode) raw.get("cbiRequest")).put("brandNewClearingAttr", "X");

        JsonNode mapped = mapper.mapClearing(raw, sampleCard(), linkage);

        assertEquals("X", mapped.at("/cbiRequest/brandNewClearingAttr").asText());
        assertTrue(mapped.has("rId"));
    }

    @Test
    void fullMapperStillValidAgainstClasspathSchema() throws Exception {
        ObjectNode raw = (ObjectNode) objectMapper.readTree(
                new ClassPathResource("fixtures/GOODS/auth.json").getInputStream());
        JsonNode mapped = mapper.mapAuthorization(raw, sampleCard(), linkage);
        assertEquals(sampleCard().getCardId(),
                mapped.at("/cbiRequest/tranRequest/parties/cust/token/0/card/cardId").asText());
        assertEquals(linkage.key(), mapped.at("/cbiRequest/tranRequest/match/key").asText());
    }

    private JsonNode extendCardTokenWith(JsonNode schema, String fieldName, String fieldType) {
        ObjectNode copy = schema.deepCopy();
        ObjectNode cardToken = (ObjectNode) copy.at("/definitions/CardToken");
        ObjectNode props = (ObjectNode) cardToken.get("properties");
        ObjectNode field = props.putObject(fieldName);
        field.put("type", fieldType);
        return copy;
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
