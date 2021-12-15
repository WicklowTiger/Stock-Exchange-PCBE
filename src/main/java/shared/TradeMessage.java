package shared;

public class TradeMessage {
    public User user = null;
    public String stockName = null;
    public String stockAmount = null;
    public String price = null;

    public TradeMessage(User user, String stockName, String stockAmount, String price) {
        this.user = user;
        this.stockName = stockName;
        this.stockAmount = stockAmount;
        this.price = price;
    }

    public TradeMessage(User user, String stockName, String stockAmount) {
        this.user = user;
        this.stockName = stockName;
        this.stockAmount = stockAmount;
    }

    public static TradeMessage fromString(User user, String str) throws Exception {
        String[] tempArr = str.split(",");
        if (tempArr.length == 2) {
            return new TradeMessage(user, tempArr[0], tempArr[1]);
        } else if (tempArr.length == 3) {
            return new TradeMessage(user, tempArr[0], tempArr[1], tempArr[2]);
        } else {
            throw new Exception("Bad trade message: Can't make trade message out of \"" + str + '"');
        }
    }

    @Override
    public String toString() {
        if (price != null) {
            return user.uid + ',' + stockName + ',' + stockAmount + ',' + price;
        } else {
            return user.uid + ',' + stockName + ',' + stockAmount;
        }
    }
}
