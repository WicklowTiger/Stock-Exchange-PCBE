package server;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import shared.MessageOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RunServer {
    public static void main(String[] args) {
        ServerConsumer<String, String> serverConsumer = new ServerConsumer<String, String>(
                new StringDeserializer(),
                new StringDeserializer(),
                new ArrayList<String>(Arrays.asList("tradeMessages"))
        );
        serverConsumer.startListening();
        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
