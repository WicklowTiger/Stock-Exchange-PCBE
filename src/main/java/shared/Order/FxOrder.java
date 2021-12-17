package shared.Order;

public class FxOrder {
    public Order order = null;
    public String stockName = null;
    public String orderType = null;

    public FxOrder(Order order, String stockName, String orderType) {
        this.order = order;
        this.stockName = stockName;
        this.orderType = orderType;
    }

    public String getStockName() {
        return stockName;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getAmount() {
        return order.amount.toString();
    }

    public String getPrice() {
        return order.price.toString();
    }

}
