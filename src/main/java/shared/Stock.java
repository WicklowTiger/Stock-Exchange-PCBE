package shared;

import java.util.ArrayList;

public class Stock implements StockModel{
    public String name;
    public Float price;
    public String companyName;
    public String marketCap;

    public final ArrayList<Order> sellOrders = new ArrayList<>();
    public final ArrayList<Order> buyOrders = new ArrayList<>();

    public Stock(String name, Float price, String companyName, String marketCap) {
        this.name = name;
        this.price = price;
        this.companyName = companyName;
        this.marketCap = marketCap;
    }

    public Stock(String name, Float price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public String getPrice() {
        return this.price.toString();
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

}
