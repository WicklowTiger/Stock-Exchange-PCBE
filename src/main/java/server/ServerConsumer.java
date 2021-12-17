package server;

import org.apache.kafka.common.serialization.Deserializer;
import shared.Const;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.Message.Message;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Processes trade messages and connection heartbeats.
 * <br>
 * Uses {@link ExchangeManager} to distribute stock
 */
public class ServerConsumer<K, V> {
    private final Logger logger = LoggerFactory.getLogger(client.ClientProducer.class);
    private final KafkaConsumer<K, V> consumer;
    private final ArrayList<String> topicsToReadFrom = new ArrayList<String>();

    public <T extends Deserializer<K>, U extends Deserializer<V>> ServerConsumer(T keyDeserializer, U valueDeserializer, ArrayList<String> topics) {
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Const.bootstrapServerIP);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer.getClass().getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer.getClass().getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "exchangeServer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<K, V>(properties);
        this.topicsToReadFrom.addAll(topics);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping server consumer...");
        }));
    }

    public void startListening() {
        this.consumer.subscribe(this.topicsToReadFrom);
        while (true) {
            ConsumerRecords<K, V> records = ServerConsumer.this.consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<K, V> record : records) {
                String key = "";
                if (record.key() != null) {
                    key = record.key().toString();
                }
                ServerActionsManager.putAction(
                        new Message<String, String>(
                                key,
                                record.value().toString(),
                                record.topic()
                        )
                );
            }
        }
    }

    public void stop() {
        if (this.consumer != null) {
            this.consumer.close();
        }
    }
}
