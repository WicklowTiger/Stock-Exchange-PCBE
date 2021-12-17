package shared;

public class Order implements Comparable<Order> {
    public String userUid;
    public Float price;
    public Float amount;
    public String stockName;

    public Order(String userUid, float price, float amount, String stockName) {
        this.userUid = userUid;
        this.price = price;
        this.amount = amount;
        this.stockName = stockName;
    }

    public Order(float price, float amount) {
        this.price = price;
        this.amount = amount;
    }

    public Order(float price, float amount, String stockName) {
        this.price = price;
        this.amount = amount;
        this.stockName = stockName;
    }

    public String getPrice() {
        return price.toString();
    }

    public String getAmount() {
        return amount.toString();
    }

    @Override
    public int compareTo(Order other) {
        return Integer.compare(0, this.price.compareTo(other.price));
    }

    @Override
    public String toString() {
        return price.toString() + '-' + amount.toString();
    }

    public static Order fromString(String stockName, String str) {
        String[] tmpArray = str.split("-");
        tmpArray[0] = tmpArray[0].replace("B", "");
        tmpArray[0] = tmpArray[0].replace("S", "");
        return new Order(Float.parseFloat(tmpArray[0]), Float.parseFloat(tmpArray[1]), stockName);
    }
}
