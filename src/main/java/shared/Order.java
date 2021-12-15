package shared;

public class Order {
    private String price;

    public Order(String price){
        this.price = price;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
}
