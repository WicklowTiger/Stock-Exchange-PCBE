package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Identifiable{
    String uid = "";
    String name = "";
    Float balance = 0f;
    Map<String, Float> stockBalance = new HashMap<>();
    ArrayList<Order> orders = new ArrayList<>();

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

}
