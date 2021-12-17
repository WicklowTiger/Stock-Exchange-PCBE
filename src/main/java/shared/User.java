package shared;

import shared.Order.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class User implements Identifiable {
    public String uid = null;
    public String name = null;
    public Float balance = null;
    public Map<String, Float> stockBalance = new HashMap<>();
    public ArrayList<Order> buyOrders = new ArrayList<>();
    public ArrayList<Order> sellOrders = new ArrayList<>();

    public User(String uid, String name, Float balance) {
        this.uid = uid;
        this.name = name;
        this.balance = balance;
        this.initStockBalance();
    }

    public void initStockBalance() {
        stockBalance.put("ALPHABET_A", 0f);
        stockBalance.put("ALPHABET_B", 0f);
        stockBalance.put("MSFT", 0f);
        stockBalance.put("AAPL", 0f);
        stockBalance.put("JNJ", 0f);
        stockBalance.put("JPM", 0f);
    }

    @Override
    public String toString() {
        StringBuilder userString = new StringBuilder();
        userString.append(name).append(",").append(balance);
        if (buyOrders.size() != 0 || sellOrders.size() != 0) {
            userString.append("!ORDERS!");
        } else {
            userString.append("!ORDERS!__NO_ORDERS__");
        }
        for (Order order : buyOrders) {
            userString.append(order.stockName).append(",B").append(order.toString()).append(';');
        }
        for (Order order : sellOrders) {
            userString.append(order.stockName).append(",S").append(order.toString()).append(';');
        }
        if (userString.charAt(userString.toString().length() - 1) == ';') {
            userString.deleteCharAt(userString.toString().length() - 1);
        }
        userString.append("!ORDERS!");
        for (Map.Entry<String, Float> entry : stockBalance.entrySet()) {
            userString.append(entry.getKey()).append(",").append(entry.getValue()).append(";");
        }
        if (userString.charAt(userString.toString().length() - 1) == ';') {
            userString.deleteCharAt(userString.toString().length() - 1);
        }
        return userString.toString();
    }

    public static User fromString(String uid, String str) {
        String[] tmpArray = str.split("!ORDERS!");
        System.out.println(Arrays.toString(tmpArray));
        User newUser = new User(uid, "", 0f);
        if (tmpArray.length >= 1) {
            String[] tempFields = tmpArray[0].split(",");
            newUser = new User(uid, tempFields[0], Float.parseFloat(tempFields[1]));
        }
        if (tmpArray.length >= 2) {
            if (!tmpArray[1].equals("__NO_ORDERS__")) {
                String[] tempOrders = tmpArray[1].split(";");
                for (String orderStr : tempOrders) {
                    String[] tempOrderFields = orderStr.split(",");
                    switch (tempOrderFields[1].charAt(0)) {
                        case 'B':
                            newUser.buyOrders.add(Order.fromString(tempOrderFields[0], tempOrderFields[1]));
                            break;
                        case 'S':
                        default:
                            newUser.sellOrders.add(Order.fromString(tempOrderFields[0], tempOrderFields[1]));
                            break;
                    }
                }
            }
        }
        if (tmpArray.length == 3) {
            String[] tempBalances = tmpArray[2].split(";");
            for (String stockBalancePair : tempBalances) {
                String[] tempBalanceFields = stockBalancePair.split(",");
                newUser.stockBalance.put(tempBalanceFields[0], Float.parseFloat(tempBalanceFields[1]));
            }
        }
        return newUser;
    }
}
