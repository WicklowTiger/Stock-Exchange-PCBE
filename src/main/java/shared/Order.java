package shared;

public class Order {
    public String userUid;
    public Float price;
    public Float amount;

    public Order(String userUid, float price, float amount){
        this.userUid = userUid;
        this.price = price;
        this.amount = amount;
    }

    public Order(float price, float amount){
        this.price = price;
        this.amount = amount;
    }

    public String getPrice() {
        return price.toString();
    }

    public String getAmount() {
        return amount.toString();
    }

    @Override
    public String toString() {
        return price.toString() + '-' + amount.toString();
    }
}
