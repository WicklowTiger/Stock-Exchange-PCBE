package shared;

public class Order {
    private String price;
    private String amount;

    public Order(String price, String amount){
        this.price = price;
        this.amount = amount;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
