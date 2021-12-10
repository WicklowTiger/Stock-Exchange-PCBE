package client;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.Const;
import shared.MessageOptions;

import java.util.Properties;

/**
 * Used by the {@link TradeManager} to send buy/sell messages to kafka.
 * <br>
 * Configuration is set when object is instantiated (check constructor).
 */
public class ClientProducer<K, V> {
    private final Logger logger = LoggerFactory.getLogger(ClientProducer.class);
    private final KafkaProducer<K, V> producer;

    public <T extends Serializer<K>, U extends Serializer<V>> ClientProducer(T keySerializer, U valueSerializer) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Const.bootstrapServerIP);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer.getClass().getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getClass().getName());

        this.producer = new KafkaProducer<K, V>(properties);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
        }));
    }

    public void sendMessage(String topic, V value, MessageOptions<K> opts) {
        producer.send(new ProducerRecord<K, V>(topic, value), (recordMetadata, e) -> {
            if (e == null) {
                logger.info("\nNew recordMetaData: \nTopic: " + recordMetadata.topic() + "\n" +
                        "Partition: " + recordMetadata.partition() + "\n" +
                        "Offset: " + recordMetadata.offset() + "\n" +
                        "Timestamp: " + recordMetadata.timestamp());
            } else {
                logger.error("Error while sending message", e);
            }
        });

        producer.flush();
    }

    public void stop() {
        producer.close();
    }
}
