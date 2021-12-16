package server;

import client.jfx.HomeWindowController;
import org.apache.kafka.common.serialization.StringDeserializer;
import shared.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerActionsManager {
    private static final ConcurrentHashMap<Integer, ServerAction> actionQueue= new ConcurrentHashMap<>();
    private static ServerActionsManager inst = null;
    private final ServerConsumer<String, String> consumer;
    private final ExchangeManager exchangeManager;
    private Thread consumerThread = null;
    private Thread pollingThread = null;

    /**Initializes exchange manager and consumer thread*/
    private ServerActionsManager() {
        consumer = new ServerConsumer<String, String>(new StringDeserializer(), new StringDeserializer(), Const.serverListenTopics);
        exchangeManager = ExchangeManager.getInstance();
        this.consumerThread = new Thread(consumer::startListening);
        this.consumerThread.start();
    }

    public static ServerActionsManager getInstance() {
        if(inst == null) {
            inst = new ServerActionsManager();
        }
        return inst;
    }

    public static ServerAction messageToAction(Message<String, String> message) {
        switch (message.topic) {
            case TRADE_MESSAGES:
                return new ServerAction(ServerActionType.RECEIVED_TRADE, message.toString());
            case KEEP_ALIVE:
                return new ServerAction(ServerActionType.SEND_USER_UPDATES, message.toString());
            default:
                return new ServerAction(ServerActionType.SEND_REPLY, "An error occured, please restart the app.");
        }
    }

    public static void putAction(Message<String, String> message) {
        putAction(messageToAction(message));
    }

    public static void putAction(ServerAction action) {
        actionQueue.put(Action.generateKey(), action);
    }

    public void run() {
        if(this.pollingThread != null) {
            this.pollingThread.interrupt();
        }
        this.pollingThread = new Thread(() -> {
            while(true) {
                if (!actionQueue.isEmpty()) {
                    Map.Entry<Integer, ServerAction> entry = actionQueue.entrySet().iterator().next();
                    switch (entry.getValue().actionType) {
                        case RECEIVED_TRADE:
                            exchangeManager.handleTrade(entry.getValue().payload);
                            break;
                        case SEND_USER_UPDATES:
                            exchangeManager.keepUpdated(entry.getValue().payload);
                            break;
                        case SEND_REPLY:
                            exchangeManager.sendReply(entry.getValue().payload);
                            break;
                        default:
                            break;
                    }
                    actionQueue.remove(entry.getKey());
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.pollingThread.start();
    }

    public void stop() {
        consumerThread.interrupt();
        pollingThread.interrupt();
        consumer.stop();
    }
}
