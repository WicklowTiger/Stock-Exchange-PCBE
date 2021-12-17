package client;

import client.jfx.HomeWindowController;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import shared.*;
import shared.Action.Action;
import shared.Action.ActionType;
import shared.Message.Message;
import shared.Message.MessageOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Singleton manager for processing and distributing client actions
 */
public class ClientActionsManager {
    private static final ConcurrentHashMap<Integer, Action> actionQueue = new ConcurrentHashMap<>();
    private static ClientProducer<String, String> heartbeatProducer;
    private static ClientActionsManager inst = null;
    private final ClientConsumer<String, String> consumer;
    private final TradeManager tradeManager;
    private Thread consumerThread = null;
    private Thread pollingThread = null;
    private Thread heartbeatThread = null;

    /**
     * Initializes trade manager and waits for homeWindowController before issuing any action
     */
    private ClientActionsManager(String userUid) {
        // Initialize heartbeat and kafka objects
        heartbeatProducer = new ClientProducer<>(new StringSerializer(), new StringSerializer(), "keepAlive");
        (this.heartbeatThread = new Thread(this::heartbeat)).start();
        consumer = new ClientConsumer<>(new StringDeserializer(), new StringDeserializer(), Const.clientListenTopics, userUid);

        // Initialize TradeManager
        tradeManager = TradeManager.getInstance();
        tradeManager.setUser(new User(userUid, "", 0f));

        // Wait for homeWindowController before starting consumer thread
        HomeWindowController homeWindowController = HomeWindowController.getInstance();
        (this.consumerThread = new Thread(consumer::startListening)).start();
    }

    public static ClientActionsManager getInstance(String userUid) {
        if (inst == null) {
            inst = new ClientActionsManager(userUid);
        }
        return inst;
    }

    /**
     * Converts kafka messages received from consumer into Actions
     *
     * @param message kafka record wrapper
     * @return {@link Action}
     */
    public static Action messageToAction(Message<String, String> message) {
        switch (message.topic) {
            case TRADE_REPLIES:
                return new Action(ActionType.ACK_REPLY, message.toStringWithKey());
            case STOCK_UPDATES:
                return new Action(ActionType.UPDATE_STOCKS, message.toString());
            case USER_UPDATES:
                return new Action(ActionType.UPDATE_USER, message.toStringWithKey());
            default:
                return new Action(ActionType.ACK_REPLY, "An error occured, please restart the app.");
        }
    }

    public static void putAction(Message<String, String> message) {
        putAction(messageToAction(message));
    }

    public static void putAction(Action action) {
        actionQueue.put(Action.generateKey(), action);
    }

    /**
     * Polls the actionQueue and decides
     * who is responsible for handling each action
     */
    public void run() {
        if (this.pollingThread != null) {
            this.pollingThread.interrupt();
        }
        this.pollingThread = new Thread(() -> {
            while (true) {
                if (!actionQueue.isEmpty()) {
                    Map.Entry<Integer, Action> entry = actionQueue.entrySet().iterator().next();
                    switch (entry.getValue().actionType) {
                        case UPDATE_STOCKS:
                            HomeWindowController.updateStocks(entry.getValue().payload);
                            break;
                        case UPDATE_USER:
                            if (tradeManager.getUser().uid.equals(entry.getValue().payload.split(",")[0])) {
                                tradeManager.updateUser(entry.getValue().payload);
                                HomeWindowController.updateUser(tradeManager.getUser());
                            }
                            break;
                        case ACK_REPLY:
                            if (tradeManager.getUser().uid.equals(entry.getValue().payload.split(",")[0])) {
                                HomeWindowController.openDialogBox(entry.getValue().payload);
                            }
                            break;
                        case SEND_BUY:
                            tradeManager.handleBuyAction(entry.getValue().payload);
                            break;
                        case SEND_SELL:
                            tradeManager.handleSellAction(entry.getValue().payload);
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
        this.consumerThread.interrupt();
        this.pollingThread.interrupt();
        this.heartbeatThread.interrupt();
        heartbeatProducer.stop();
        consumer.stop();
    }

    /**
     * Sends a message to the server containing the user's uid
     * This will keep the user connected making him receive updates
     */
    private void heartbeat() {
        while (true) {
            if (tradeManager != null && tradeManager.getUser() != null) {
                heartbeatProducer.sendMessage(tradeManager.getUser().uid, new MessageOptions<>());
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
