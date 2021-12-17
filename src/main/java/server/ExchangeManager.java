package server;

import org.apache.kafka.common.serialization.StringSerializer;
import shared.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Singleton manager that handles all exchange operations dictated
 * by messages received on the kafka consumer {@link ServerConsumer}
 */
public class ExchangeManager implements InvocationHandler {
    private static final ConcurrentHashMap<String, Integer> connectedUsers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Stock> stockDatabase = new ConcurrentHashMap<>();
    private static ExchangeManager inst = null;
    private static ServerProducer<String, String> serverProducer;
    private Thread connectionThread = null;

    /**
     * Initializes a mock database + starts serverProducer and connectionThread
     */
    private ExchangeManager() {
        initDatabase();
        serverProducer = new ServerProducer<String, String>(new StringSerializer(), new StringSerializer());
        connectionThread = new Thread(this::ageConnections);
        connectionThread.start();
    }

    public static ExchangeManager getInstance() {
        if (inst == null) {
            inst = new ExchangeManager();
        }
        return inst;
    }

    /**
     * Called on arrival of user heartbeat message
     * Resets his disconnect timer
     * Sends him user updates {@link User}.toString()
     */
    public void keepUpdated(String message) {
        if (!connectedUsers.containsKey(message)) {
            System.out.println("User " + message + " connected to the server!");
        }
        connectedUsers.put(message, 0);
        userDatabase.get(message).sellOrders.clear();
        userDatabase.get(message).buyOrders.clear();
        for (Map.Entry<String, Stock> entry : stockDatabase.entrySet()) {
            for (Order order : entry.getValue().sellOrders) {
                if (order.userUid.equals(message)) {
                    userDatabase.get(message).sellOrders.add(order);
                }
            }
            for (Order order : entry.getValue().buyOrders) {
                if (order.userUid.equals(message)) {
                    userDatabase.get(message).buyOrders.add(order);
                }
            }
        }
        serverProducer.sendMessage("userUpdates", userDatabase.get(message).toString(), new MessageOptions<String>(message));
    }

    /**
     * Reply to specific users
     */
    public void sendReply(String userUid, String message) {
        serverProducer.sendMessage("tradeReplies", message, new MessageOptions<String>(userUid));
    }

    /**
     * Broadcast reply?!
     */
    public void sendReply(String message) {
        serverProducer.sendMessage("tradeReplies", message, new MessageOptions<>());
    }

    /**
     * Handles trade message received from kafka
     */
    public void handleTrade(String message) {
        try {
            handleTrade(Trade.fromString(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleTrade(Trade trade) {
        try {
            this.checkTrade(trade);
        } catch (Throwable e) {
            sendReply(trade.userUid, e.toString());
            return;
        }
        switch (trade.tradeType) {
            case MARKET_SELL:
                handleMarketSell(trade);
                break;
            case MARKET_BUY:
                handleMarketBuy(trade);
                break;
            case LIMIT_SELL:
                handleLimitSell(trade);
                break;
            case LIMIT_BUY:
                handleLimitBuy(trade);
                break;
            default:
                break;
        }
    }

    public void stop() {
        connectionThread.interrupt();
        serverProducer.stop();
    }

    private void giveUserStock(String uid, String stockName, Float amount) {
        Float currentAmount = userDatabase.get(uid).stockBalance.get(stockName);
        userDatabase.get(uid).stockBalance.put(stockName, currentAmount + amount);
    }

    private void giveUserBalance(String uid, Float amount) {
        userDatabase.get(uid).balance += amount;
    }

    private void withdrawStockFromUser(String uid, String stockName, Float amount) {
        Float currentAmount = userDatabase.get(uid).stockBalance.get(stockName);
        userDatabase.get(uid).stockBalance.put(stockName, currentAmount - amount);
    }

    private void withdrawBalanceFromUser(String uid, Float amount) {
        userDatabase.get(uid).balance -= amount;
    }

    /**
     * Places a limit buy order
     * If price got changed and is < limit changes to market order
     */
    private void handleLimitBuy(Trade trade) {
        if (trade.stockPrice > stockDatabase.get(trade.stockName).price) {
            handleMarketBuy(trade);
            return;
        }
        stockDatabase.get(trade.stockName).buyOrders.add(new Order(trade.userUid, trade.stockPrice, trade.stockAmount, trade.stockName));
        Collections.sort(stockDatabase.get(trade.stockName).buyOrders);
        withdrawBalanceFromUser(trade.userUid, trade.stockAmount * trade.stockPrice);
    }

    /**
     * Recursive market buy function
     * Can only handle 1 sell order at once
     * Voodoo
     */
    private void handleMarketBuy(Trade trade) {
        Stock wantedStock = stockDatabase.get(trade.stockName);
        ArrayList<Order> sellOrders = wantedStock.sellOrders;
        if (trade.stockAmount >= sellOrders.get(0).amount) { // fully fills order
            trade.stockAmount -= sellOrders.get(0).amount;
            float stockToGive = sellOrders.get(0).amount;
            float balanceToTake = sellOrders.get(0).price * sellOrders.get(0).amount;
            giveUserStock(trade.userUid, trade.stockName, stockToGive);
            withdrawBalanceFromUser(trade.userUid, balanceToTake);
            giveUserBalance(sellOrders.get(0).userUid, balanceToTake);
            sendReply(trade.userUid, "Partially filled  " + trade.stockName + " buy for " + stockToGive + " (" + balanceToTake + "$)");
            sendReply(sellOrders.get(0).userUid, "Filled  " + trade.stockName + " sell for " + balanceToTake + "$" + " (" + stockToGive + trade.stockName + ")");
            sellOrders.remove(0);
            if (Math.round(trade.stockAmount) == 0) {
                return;
            }
            handleTrade(trade);
        } else { // partially fills order
            sellOrders.get(0).amount -= trade.stockAmount;
            float stockToGive = trade.stockAmount;
            float balanceToTake = sellOrders.get(0).price * trade.stockAmount;
            giveUserStock(trade.userUid, trade.stockName, stockToGive);
            withdrawBalanceFromUser(trade.userUid, balanceToTake);
            giveUserBalance(sellOrders.get(0).userUid, balanceToTake);
            sendReply(trade.userUid, "Filled  " + trade.stockName + " buy for " + stockToGive + " (" + balanceToTake + "$)");
            sendReply(sellOrders.get(0).userUid, "Partially filled  " + trade.stockName + " sell for " + balanceToTake + "$" + " (" + stockToGive + trade.stockName + ")");
        }
    }

    /**
     * Places a limit sell order
     * If price got changed and is > limit changes to market order
     */
    private void handleLimitSell(Trade trade) {
        if (trade.stockPrice < stockDatabase.get(trade.stockName).price) {
            handleMarketSell(trade);
            return;
        }
        stockDatabase.get(trade.stockName).sellOrders.add(new Order(trade.userUid, trade.stockPrice, trade.stockAmount, trade.stockName));
        Collections.sort(stockDatabase.get(trade.stockName).sellOrders);
        Collections.reverse(stockDatabase.get(trade.stockName).sellOrders);
        withdrawStockFromUser(trade.userUid, trade.stockName, trade.stockAmount);
    }

    /**
     * Recursive market sell function
     * Can only handle 1 buy order at once
     * Voodoo
     */
    private void handleMarketSell(Trade trade) {
        Stock wantedStock = stockDatabase.get(trade.stockName);
        ArrayList<Order> buyOrders = wantedStock.buyOrders;
        if (trade.stockAmount >= buyOrders.get(0).amount) { // fully fills order
            trade.stockAmount -= buyOrders.get(0).amount;
            float stockToGive = buyOrders.get(0).amount;
            float balanceToTake = buyOrders.get(0).price * buyOrders.get(0).amount;
            giveUserStock(buyOrders.get(0).userUid, trade.stockName, stockToGive);
            giveUserBalance(trade.userUid, balanceToTake);
            withdrawStockFromUser(trade.userUid, trade.stockName, stockToGive);
            sendReply(buyOrders.get(0).userUid, "Filled  " + trade.stockName + " buy for " + stockToGive + " (" + balanceToTake + "$)");
            sendReply(trade.userUid, "Partially filled  " + trade.stockName + " sell for " + balanceToTake + "$" + " (" + stockToGive + trade.stockName + ")");
            buyOrders.remove(0);
            if (Math.round(trade.stockAmount) == 0) {
                return;
            }
            handleTrade(trade);
        } else { // partially fills order
            buyOrders.get(0).amount -= trade.stockAmount;
            float stockToGive = trade.stockAmount;
            float balanceToTake = buyOrders.get(0).price * trade.stockAmount;
            giveUserStock(buyOrders.get(0).userUid, trade.stockName, stockToGive);
            giveUserBalance(trade.userUid, balanceToTake);
            withdrawStockFromUser(trade.userUid, trade.stockName, stockToGive);
            sendReply(buyOrders.get(0).userUid, "Partially filled  " + trade.stockName + " buy for " + stockToGive + " (" + balanceToTake + "$)");
            sendReply(trade.userUid, "Filled  " + trade.stockName + " sell for " + balanceToTake + "$" + " (" + stockToGive + trade.stockName + ")");
        }
    }

    /**
     * Called by ageConnections every second
     * Updates stock price on display ((first buy order + first sell order) / 2)
     */
    private void updateAllStockPrices() {
        for (Map.Entry<String, Stock> entry : stockDatabase.entrySet()) {
            if (entry.getValue().sellOrders.size() != 0 && entry.getValue().buyOrders.size() != 0) {
                entry.getValue().adjustPrice((entry.getValue().sellOrders.get(0).price + entry.getValue().buyOrders.get(0).price) / 2);
            }
        }
    }

    /**
     * Called by ageConnections every second
     * Broadcasts stock updates to every listening kafka consumer on stockUpdates topic
     */
    private void broadcastStockUpdates() {
        StringBuilder broadcastMessage = new StringBuilder();
        for (Map.Entry<String, Stock> entry : stockDatabase.entrySet()) {
            broadcastMessage.append(entry.getValue().name).append(',').append(entry.getValue().price).append(";");
        }
        broadcastMessage.deleteCharAt(broadcastMessage.toString().length() - 1);
        broadcastMessage.append("!ORDERS!");
        for (Map.Entry<String, Stock> entry : stockDatabase.entrySet()) {
            if (entry.getValue().buyOrders.size() == 0 && entry.getValue().sellOrders.size() == 0) {
                continue;
            }
            broadcastMessage.append(entry.getValue().name).append(',');
            for (Order order : entry.getValue().buyOrders) {
                broadcastMessage.append('B').append(order.toString()).append(',');
            }
            for (Order order : entry.getValue().sellOrders) {
                broadcastMessage.append('S').append(order.toString()).append(',');
            }
            broadcastMessage.deleteCharAt(broadcastMessage.toString().length() - 1);
            broadcastMessage.append(';');
        }
        serverProducer.sendMessage("stockUpdates", broadcastMessage.toString(), new MessageOptions<>());
    }

    /**
     * Increment user alive time every second
     * If no heartbeat received in the last 30 seconds, disconnect user
     */
    private void ageConnections() {
        while (true) {
            for (Map.Entry<String, Integer> entry : connectedUsers.entrySet()) {
                System.out.println("User " + entry.getKey() + " is online and will be disconnected in " +
                        (Const.keepAliveThreshold - entry.getValue()) + " seconds.");
                Integer new_value = entry.getValue() + 1;
                if (new_value >= Const.keepAliveThreshold) {
                    System.out.println("User " + entry.getKey() + " disconnected.");
                    connectedUsers.remove(entry.getKey());
                } else {
                    connectedUsers.put(entry.getKey(), new_value);
                }
            }
            this.updateAllStockPrices();
            this.broadcastStockUpdates();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Simple mock database
     */
    private void initDatabase() {
        userDatabase.put(Const.defaultUser.uid, Const.defaultUser);
        userDatabase.put(Const.defaultUser2.uid, Const.defaultUser2);
        stockDatabase.put("ALPHABET_A", new Stock("ALPHABET_A", 30f, "Google", "2000"));
        stockDatabase.put("ALPHABET_B", new Stock("ALPHABET_B", 20f, "Google", "3000"));
        stockDatabase.put("MSFT", new Stock("MSFT", 25f, "Microsoft", "4000"));
        stockDatabase.put("AAPL", new Stock("AAPL", 40f, "Apple", "5000"));
        stockDatabase.put("JNJ", new Stock("JNJ", 50f, "Johnson and Johnson", "6000"));
        stockDatabase.put("JPM", new Stock("JPM", 60f, "JPMorgan", "7000"));
        stockDatabase.get("MSFT").sellOrders.add(new Order(Const.defaultUser2.uid, 35f, 0.5f, "MSFT"));
        stockDatabase.get("MSFT").sellOrders.add(new Order(Const.defaultUser2.uid, 36f, 2.03f, "MSFT"));
        stockDatabase.get("AAPL").sellOrders.add(new Order(Const.defaultUser2.uid, 41f, 3f, "AAPL"));
        stockDatabase.get("AAPL").sellOrders.add(new Order(Const.defaultUser2.uid, 42f, 2.5f, "AAPL"));
        stockDatabase.get("AAPL").buyOrders.add(new Order(Const.defaultUser2.uid, 39f, 2.5f, "AAPL"));
    }

    /**
     * Gets called every time a trade is attempted
     * Error messages get sent back to the client as an alert
     *
     * @param trade or what remains of it
     */
    private void checkTrade(Trade trade) throws Throwable {
        switch (trade.tradeType) {
            case MARKET_BUY:
                if (stockDatabase.get(trade.stockName).sellOrders.size() == 0) {
                    throw new Exception(ErrorMessage.NO_ORDERS.toString());
                }
                if (userDatabase.get(trade.userUid).balance < stockDatabase.get(trade.stockName).sellOrders.get(0).price * trade.stockAmount) {
                    throw new Exception(ErrorMessage.INSUFFICIENT_FUNDS.toString());
                }
                break;
            case LIMIT_BUY:
                if (userDatabase.get(trade.userUid).balance < trade.stockPrice * trade.stockAmount) {
                    throw new Exception(ErrorMessage.INSUFFICIENT_FUNDS.toString());
                }
                break;
            case MARKET_SELL:
                if (stockDatabase.get(trade.stockName).buyOrders.size() == 0) {
                    throw new Exception(ErrorMessage.NO_ORDERS.toString());
                }
            case LIMIT_SELL:
                if (userDatabase.get(trade.userUid).stockBalance.get(trade.stockName) < trade.stockAmount) {
                    throw new Exception(ErrorMessage.INSUFFICIENT_STOCK.toString());
                }
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
                case "handleMarketSell":
                case "handleLimitBuy":
                case "handleLimitSell":
                    this.checkTrade((Trade) args[0]);
                    break;
                default:
                    break;
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
