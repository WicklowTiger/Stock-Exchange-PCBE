package shared;

import java.util.ArrayList;
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
        return name + "," + balance;
    }

    public static User fromString(String uid, String str) {
        String[] tmpArray = str.split(",");
        return new User(uid, tmpArray[0], Float.parseFloat(tmpArray[1]));
    }
}
