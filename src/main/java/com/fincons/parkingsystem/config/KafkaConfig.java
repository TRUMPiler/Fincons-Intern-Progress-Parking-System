package com.fincons.parkingsystem.config;

import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.dto.VehicleEnteredEvent;
import com.fincons.parkingsystem.dto.VehicleExitedEvent;
import com.fincons.parkingsystem.utils.ResourceFileUtil;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up Apache Kafka producers and consumers.
 * This class defines the necessary beans for serialization, deserialization, and factory configurations.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;



    @Value("${spring.kafka.properties.ssl.truststore.password}")
    private String truststorePassword;

    String truststorePath =
            ResourceFileUtil.copyToTempFile("kafka/truststore.jks", ".jks");

    String keystorePath =
            ResourceFileUtil.copyToTempFile("kafka/keystore.p12", ".p12");

    @Value("${spring.kafka.properties.ssl.keystore.password}")
    private String keystorePassword;

    @Value("${spring.kafka.properties.ssl.key.password}")
    private String keyPassword;

    @Value("${spring.kafka.properties.ssl.keystore.type}")
    private String keystoreType;



    /**
     * Creates a KafkaTemplate bean for producing messages.
     *
     * @param producerFactory The producer factory to be used by the template.
     * @return A configured KafkaTemplate.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Configures the producer factory with server and serializer properties.
     *
     * @return A configured ProducerFactory.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // SSL Configuration
        props.put("security.protocol", securityProtocol);
        props.put("ssl.truststore.location", truststorePath);
        props.put("ssl.truststore.password", truststorePassword);
        props.put("ssl.keystore.location", keystorePath);
        props.put("ssl.keystore.password", keystorePassword);
        props.put("ssl.key.password", keyPassword);
        props.put("ssl.keystore.type", keystoreType);

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Configures the consumer factory for VehicleEnteredEvent messages.
     *
     * @return A configured ConsumerFactory for VehicleEnteredEvent.
     */
    @Bean
    public ConsumerFactory<String, VehicleEnteredEvent> vehicleEntryConsumerFactory() {
        JsonDeserializer<VehicleEnteredEvent> deserializer = new JsonDeserializer<>(VehicleEnteredEvent.class);
        deserializer.addTrustedPackages("com.fincons.parkingsystem.dto");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = getConsumerProps();
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(deserializer));
    }

    /**
     * Creates a listener container factory for VehicleEnteredEvent consumers.
     *
     * @param consumerFactory The consumer factory to be used by the listener.
     * @return A configured ConcurrentKafkaListenerContainerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VehicleEnteredEvent> vehicleEnteredKafkaListenerFactory(ConsumerFactory<String, VehicleEnteredEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, VehicleEnteredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * Configures the consumer factory for VehicleExitedEvent messages.
     *
     * @return A configured ConsumerFactory for VehicleExitedEvent.
     */
    @Bean
    public ConsumerFactory<String, VehicleExitedEvent> vehicleExitConsumerFactory() {
        JsonDeserializer<VehicleExitedEvent> deserializer = new JsonDeserializer<>(VehicleExitedEvent.class);
        deserializer.addTrustedPackages("com.fincons.parkingsystem.dto");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = getConsumerProps();
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(deserializer));
    }

    /**
     * Creates a listener container factory for VehicleExitedEvent consumers.
     *
     * @return A configured ConcurrentKafkaListenerContainerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VehicleExitedEvent> vehicleExitedKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VehicleExitedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(vehicleExitConsumerFactory());
        return factory;
    }

    /**
     * Configures the consumer factory for SlotStatusUpdateDto messages.
     *
     * @return A configured ConsumerFactory for SlotStatusUpdateDto.
     */
    @Bean
    public ConsumerFactory<String, SlotStatusUpdateDto> slotUpdateConsumerFactory() {
        JsonDeserializer<SlotStatusUpdateDto> deserializer = new JsonDeserializer<>(SlotStatusUpdateDto.class);
        deserializer.addTrustedPackages("com.fincons.parkingsystem.dto");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = getConsumerProps();
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(deserializer));
    }

    /**
     * Creates a listener container factory for SlotStatusUpdateDto consumers.
     *
     * @return A configured ConcurrentKafkaListenerContainerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SlotStatusUpdateDto> slotUpdateKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SlotStatusUpdateDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(slotUpdateConsumerFactory());
        return factory;
    }

    /**
     * Helper method to create a map of common consumer properties.
     *
     * @return A map of consumer properties.
     */
    private Map<String, Object> getConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "group_id");
        props.put("auto.offset.reset", "earliest");

        // SSL Configuration
        props.put("security.protocol", securityProtocol);
        props.put("ssl.truststore.location", truststorePath);
        props.put("ssl.truststore.password", truststorePassword);
        props.put("ssl.keystore.location", keystorePath);
        props.put("ssl.keystore.password", keystorePassword);
        props.put("ssl.key.password", keyPassword);
        props.put("ssl.keystore.type", keystoreType);
        return props;
    }
}
