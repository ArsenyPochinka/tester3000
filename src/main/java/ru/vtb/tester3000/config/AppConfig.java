package ru.vtb.tester3000.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;
import ru.vtb.tester3000.mapper.LinkageKeyGenerator;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    LinkageKeyGenerator linkageKeyGenerator() {
        return new LinkageKeyGenerator();
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.findModulesViaServiceLoader(true);
    }

    @Bean(name = "regressionExecutor")
    Executor regressionExecutor(TesterProperties properties) {
        int parallel = Math.max(1, properties.getParallelTests());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(parallel);
        executor.setMaxPoolSize(parallel);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("regression-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "regressionScheduler")
    Executor regressionScheduler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("regression-sched-");
        executor.initialize();
        return executor;
    }

    // --- Clearing Kafka: spring.kafka.bootstrap-servers ---

    @Bean
    NewTopic clearingTopic(TesterProperties properties) {
        return TopicBuilder.name(properties.getKafka().getClearingTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    ProducerFactory<String, JsonNode> clearingProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        DefaultKafkaProducerFactory<String, JsonNode> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    KafkaTemplate<String, JsonNode> clearingKafkaTemplate(ProducerFactory<String, JsonNode> clearingProducerFactory) {
        return new KafkaTemplate<>(clearingProducerFactory);
    }

    // --- Fin outbox Kafka 25/104/39: tester3000.kafka.fin-bootstrap-servers ---

    @Bean
    ProducerFactory<String, String> finOutboxProducerFactory(TesterProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getFinBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    KafkaTemplate<String, String> finOutboxKafkaTemplate(
            @Qualifier("finOutboxProducerFactory") ProducerFactory<String, String> finOutboxProducerFactory
    ) {
        return new KafkaTemplate<>(finOutboxProducerFactory);
    }

    @Bean
    ConsumerFactory<String, String> finOutboxConsumerFactory(TesterProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getFinBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> finOutboxKafkaListenerContainerFactory(
            @Qualifier("finOutboxConsumerFactory") ConsumerFactory<String, String> finOutboxConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(finOutboxConsumerFactory);
        return factory;
    }
}
