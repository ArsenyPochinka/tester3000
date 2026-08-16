package ru.vtb.tester3000.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.vtb.tester3000.config.TesterProperties;

import java.util.concurrent.TimeUnit;

@Component
public class ClearingKafkaPublisher {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;
    private final TesterProperties properties;

    public ClearingKafkaPublisher(KafkaTemplate<String, JsonNode> kafkaTemplate, TesterProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public record PublishResult(boolean success, String detail) {
    }

    public PublishResult publish(JsonNode clearingMessage) {
        String topic = properties.getKafka().getClearingTopic();
        String key = clearingMessage.path("rId").asText(null);
        try {
            var result = kafkaTemplate.send(topic, key, clearingMessage).get(30, TimeUnit.SECONDS);
            var meta = result.getRecordMetadata();
            return new PublishResult(
                    true,
                    "topic=" + meta.topic() + ", partition=" + meta.partition() + ", offset=" + meta.offset()
            );
        } catch (Exception ex) {
            return new PublishResult(false, ex.getMessage());
        }
    }
}
