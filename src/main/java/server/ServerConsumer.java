package server;

import org.apache.kafka.common.serialization.Serializer;
import shared.Const;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Processes trade messages and connection heartbeats.
 * <br>
 * Uses {@link ExchangeManager} to distribute stock
 */
public class ServerConsumer<K, V> {
    private final Logger logger = LoggerFactory.getLogger(client.ClientProducer.class);
    private final KafkaConsumer<K, V> consumer;
    private Thread tradingThread;
    /**Implement this as a class with a timeout field*/
    private Thread keepAliveThread;

    public <T extends Serializer<K>, U extends Serializer<V>> ServerConsumer(T keyDeserializer, U valueDeserializer) {
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Const.bootstrapServerIP);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer.getClass().getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer.getClass().getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "exchangeServer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<K, V>(properties);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
        }));
    }

    /**TODO*/
    public void startListening() {
        this.consumer.subscribe(Arrays.asList(Const.topicMap.get("tradeTopic")));
        this.tradingThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    ConsumerRecords<K, V> records = ServerConsumer.this.consumer.poll(Duration.ofMillis(100));
                    for(ConsumerRecord<K, V> record: records) {
                        logger.info("\nKey: " + record.key() + ", Value: " + record.value());
                        logger.info("\nOffset: " + record.offset() + ", Partition: " + record.partition());
                    }
                }
            }
        };
        this.tradingThread.start();
    }

    /**TODO*/
    public void stopListening() {
        this.tradingThread.interrupt();
    }
}
