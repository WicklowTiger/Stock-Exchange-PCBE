package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Identifiable {
    public String uid = null;
    public String name = null;
    public Float balance = null;
    public Map<String, Float> stockBalance = new HashMap<>();
    public ArrayList<Order> orders = new ArrayList<>();

    public User(String uid, String name, Float balance, Map<String, Float> stockBalance, ArrayList<Order> orders) {
        this.uid = uid;
        this.name = name;
        this.balance = balance;
        this.stockBalance = stockBalance;
        this.orders = orders;
    }

    public User(String uid, String name, Float balance) {
        this.uid = uid;
        this.name = name;
        this.balance = balance;
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
