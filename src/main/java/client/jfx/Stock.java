package client.jfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Stock {

    private  String name;
    private  String price;
    private String companyName;
    private String marketcap;


    public Stock(String name, String price, String companyName, String marketcap) {
        this.name = name;
        this.price = price;
        this.companyName = companyName;
        this.marketcap = marketcap;
    }
    public String getName() {
        return this.name;
    }

    public String getPrice() {
        return this.price;
    }

    public String getMarketcap() {
        return marketcap;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public void setMarketcap(String marketcap) {
        this.marketcap = marketcap;
    }






}
