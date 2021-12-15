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
 * Handles all trade actions by using the kafka producer {@link ClientProducer}
 */
class TradeManager implements InvocationHandler {
    private static TradeManager inst = null;
    private static ClientProducer<String, String> clientProducer;
    private User user;

    private TradeManager() {
        clientProducer = new ClientProducer<String, String>(new StringSerializer(), new StringSerializer(), "tradeMessages");
    }

    public static TradeManager getInstance() {
        if(inst == null){
            inst = new TradeManager();
        }
        return inst;
    }

    public void handleSellAction(String message) {
        try {
            TradeMessage tradeMessage = TradeMessage.fromString(this.user, message);
            sendSell(tradeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleBuyAction(String message) {
        try {
            TradeMessage tradeMessage = TradeMessage.fromString(this.user, message);
            sendBuy(tradeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void sendBuy(TradeMessage tradeMessage) {
        if(tradeMessage.price == null) {
            clientProducer.sendMessage(TradeAction.MARKET_BUY.toString() + ',' + tradeMessage, new MessageOptions<String>());
        } else {
            clientProducer.sendMessage(TradeAction.LIMIT_BUY.toString() + ',' + tradeMessage, new MessageOptions<String>());
        }
    }

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



