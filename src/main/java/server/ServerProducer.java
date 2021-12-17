package server;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.Const;
import shared.MessageOptions;

import java.util.Properties;


/**
 * Sends trade replies and stock updates after each trade
 */
public class ServerProducer<K, V> {
    private final Logger logger = LoggerFactory.getLogger(ServerProducer.class);
    private final KafkaProducer<K, V> producer;

    public <T extends Serializer<K>, U extends Serializer<V>> ServerProducer(T keySerializer, U valueSerializer) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Const.bootstrapServerIP);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer.getClass().getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getClass().getName());

        this.producer = new KafkaProducer<K, V>(properties);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping server producer...");
        }));
    }

    public void sendMessage(String topic, V value, MessageOptions<K> opts) {
        new Thread(() -> {
            if (opts.key != null) {
                producer.send(new ProducerRecord<K, V>(topic, opts.key, value));
            } else {
                producer.send(new ProducerRecord<K, V>(topic, value));
            }
            producer.flush();
        }).start();
    }

    public void stop() {
        producer.close();
    }

    void callBackFn(RecordMetadata recordMetadata, Exception e) {
        if (e == null) {
            logger.info("\nNew recordMetaData: \nTopic: " + recordMetadata.topic() + "\n" +
                    "Partition: " + recordMetadata.partition() + "\n" +
                    "Offset: " + recordMetadata.offset() + "\n" +
                    "Timestamp: " + recordMetadata.timestamp());
        } else {
            logger.error("Error while sending message", e);
        }
    }
}
