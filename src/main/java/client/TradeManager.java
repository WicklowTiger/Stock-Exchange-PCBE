package client;

import org.apache.kafka.common.serialization.StringSerializer;
import shared.MessageOptions;
import shared.TradeAction;
import shared.TradeMessage;
import shared.User;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Singleton manager that handles all trade actions by using the kafka producer {@link ClientProducer}
 */
class TradeManager implements InvocationHandler {
    private static TradeManager inst = null;
    private static ClientProducer<String, String> clientProducer;
    private User user;

    /**Initializes clientProducer for trade messages*/
    private TradeManager() {
        clientProducer = new ClientProducer<>(new StringSerializer(), new StringSerializer(), "tradeMessages");
    }

    public static TradeManager getInstance() {
        if(inst == null){
            inst = new TradeManager();
        }
        return inst;
    }

    /**
     * Handles sell messages coming from UI
     * @param message (ex. MARKET_SELL,AAPL,35.0,3 - TradeType, Ticker, Price?, Amount)
     */
    public void handleSellAction(String message) {
        try {
            TradeMessage tradeMessage = TradeMessage.fromString(this.user, message);
            sendSell(tradeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles buy messages coming from UI
     * @param message (ex. MARKET_BUY,AAPL,35.0,3 - TradeType, Ticker, Price?, Amount)
     */
    public void handleBuyAction(String message) {
        try {
            TradeMessage tradeMessage = TradeMessage.fromString(this.user, message);
            sendBuy(tradeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * See {@link User} fromString and toString for message formats
     */
    public void updateUser(String message) {
        String[] tmpArray = message.split(",", 2);
        setUser(User.fromString(tmpArray[0], tmpArray[1]));
    }

    public void setUser(User user) {
        System.out.println("setting user to " + user.toString());
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    /**
     * Checks if order is market or limit and sends formatted message to kafka server
     */
    public void sendBuy(TradeMessage tradeMessage) {
        if(tradeMessage.price == null) {
            clientProducer.sendMessage(TradeAction.MARKET_BUY.toString() + ',' + tradeMessage, new MessageOptions<String>());
        } else {
            clientProducer.sendMessage(TradeAction.LIMIT_BUY.toString() + ',' + tradeMessage, new MessageOptions<String>());
        }
    }

    /**
     * Checks if order is market or limit and sends formatted message to kafka server
     */
    public void sendSell(TradeMessage tradeMessage) {
        if(tradeMessage.price == null) {
            clientProducer.sendMessage(TradeAction.MARKET_SELL.toString() + ',' + tradeMessage, new MessageOptions<String>());
        } else {
            clientProducer.sendMessage(TradeAction.LIMIT_SELL.toString() + ',' + tradeMessage, new MessageOptions<String>());
        }
    }

    public void stop() {
        clientProducer.stop();
    }

    private void checkUserExists() throws Throwable {
        if(this.user == null) throw new Exception("User unknown. Please set user after instantiating TradeManager.");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            if (method.getName().startsWith("send")) {
                this.checkUserExists();
            }
            result = method.invoke(this, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("Invocation Exception: " + e.getMessage());
        }
        return result;
    }
}



