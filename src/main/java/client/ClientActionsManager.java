package client;

import client.jfx.App;
import client.jfx.HomeWindowController;
import javafx.application.Application;
import shared.Action;
import shared.ActionType;
import shared.Const;
import shared.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClientActionsManager {
    private static final ConcurrentHashMap<Integer, Action> actionQueue= new ConcurrentHashMap<>();
    private static ClientActionsManager inst = null;
    private final TradeManager tradeManager;
    /**This is not trade-safe, need actionQueue mechanism or new thread?*/
    private final HomeWindowController homeWindowController;
    private Thread pollingThread = null;

    private ClientActionsManager() {
        tradeManager = TradeManager.getInstance();
        tradeManager.setUser(Const.defaultUser);
        homeWindowController = HomeWindowController.getInstance();
    }

    public static ClientActionsManager getInstance() {
        if(inst == null) {
            inst = new ClientActionsManager();
        }
        return inst;
    }

    public static Action messageToAction(Message<String, String> message) {
        switch (message.topic) {
            case TRADE_REPLIES:
                return new Action(ActionType.ACK_REPLY, message.toString());
            case STOCK_UPDATES:
                return new Action(ActionType.UPDATE_STOCKS, message.toString());
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

    public void run() {
        if(this.pollingThread != null) {
            this.pollingThread.interrupt();
        }
        this.pollingThread = new Thread(() -> {
            while(true) {
                if (!actionQueue.isEmpty()) {
                    Map.Entry<Integer, Action> entry = actionQueue.entrySet().iterator().next();
                    switch (entry.getValue().actionType) {
                        case UPDATE_STOCKS:
                            System.out.println("Update stocks");
                            break;
                        case ACK_REPLY:
                            System.out.println("Ack reply");
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
}
