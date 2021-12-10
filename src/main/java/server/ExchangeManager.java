package server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

enum ErrorMessage {
    INSUFFICIENT_FUNDS,
    INSUFFICIENT_STOCK,
    UNKNOWN_ERROR,
}

enum TradeAction {
    MARKET_BUY,
    MARKET_SELL,
    LIMIT_BUY,
    LIMIT_SELL,
}

/**
 * Handles all exchange operations dictated by messages
 * received by the kafka consumer {@link ServerConsumer}
 */
public class ExchangeManager implements InvocationHandler {
    private static ExchangeManager inst = null;
    private Map<Object, Object> stockMap;

    /**TODO*/
    private ExchangeManager() {
       //this.initData();
        stockMap = new HashMap<Object, Object>() {{
            put("stock1", 5);
            put("stock2", 25);
            put("stock3", 35);
            put("stock4", 55);
        }};
    }

    public static ExchangeManager getInstance() {
        if(inst == null){
            inst = new ExchangeManager();
        }
        return inst;
    }

    public static void handleTrade() {

    }

    private void handleLimitBuy() {

    }

    private void handleMarketBuy() {

    }

    private void handleBuy() {

    }

    private void handleLimitSell() {

    }

    private void handleMarketSell() {

    }

    private void handleSell() {

    }

    /**TODO*/
    private void checkTrade(TradeAction action, Object[] args) throws Throwable {
        switch (action) {
            case MARKET_BUY:
                break;
            case MARKET_SELL:
                break;
            case LIMIT_BUY:
                break;
            case LIMIT_SELL:
                break;
            default:
                throw new Exception(ErrorMessage.UNKNOWN_ERROR.toString());
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            switch (method.getName()) {
                case "handleMarketBuy":
                    this.checkTrade(TradeAction.MARKET_BUY, args);
                    break;
                case "handleMarketSell":
                    this.checkTrade(TradeAction.MARKET_SELL, args);
                    break;
                case "handleLimitBuy":
                    this.checkTrade(TradeAction.LIMIT_BUY, args);
                    break;
                case "handleLimitSell":
                    this.checkTrade(TradeAction.LIMIT_SELL, args);
                    break;
                default: break;
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
