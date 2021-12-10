package client;

import org.apache.kafka.common.serialization.StringSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handles all trade actions by using the kafka producer {@link ClientProducer}
 */
class TradeManager implements InvocationHandler {
    private static TradeManager inst = null;
    private static ClientProducer<String, String> clientProducer;
    /** user type TBD */
    private Object user;

    private TradeManager() {
        clientProducer = new ClientProducer<String, String>(new StringSerializer(), new StringSerializer());
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public void buyStock(String stockName, Float amount, Float price) {
        if(price == null) {
            System.out.println("market buy order");
        } else {
            System.out.println("limit buy order");
        }
    }

    public void buyStock(String stockName, Float amount) {
        buyStock(stockName, amount, null);
    }

    public void sellStock(String stockName, Float amount, Float price) {
        if(price == null) {
            System.out.println("market sell order");
        } else {
            System.out.println("limit sell order");
        }
    }

    public void sellStock(String stockName, Float amount) {
        sellStock(stockName, amount, null);
    }

    public static TradeManager getInstance() {
        if(inst == null){
            inst = new TradeManager();
        }
        return inst;
    }

    public void exit() {
        clientProducer.stop();
    }

    private void checkUserExists() throws Throwable {
        if(this.user == null) throw new Exception("User unknown. Please set user after instantiating TradeManager.");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            if (method.getName().endsWith("Stock")) {
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



