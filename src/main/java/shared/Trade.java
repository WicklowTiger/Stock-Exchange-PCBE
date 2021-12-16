package shared;

/**
 * {@link server.ExchangeManager} uses this object to handle trade messages
 */
public class Trade {
    public TradeAction tradeType = null;
    public String userUid = null;
    public String stockName = null;
    public Float stockAmount = null;
    public Float stockPrice = null;

    public Trade(TradeAction tradeType, String userUid, String stockName, Float stockAmount, Float stockPrice) {
        this.tradeType = tradeType;
        this.userUid = userUid;
        this.stockName = stockName;
        this.stockAmount = stockAmount;
        this.stockPrice = stockPrice;
    }

    public Trade(TradeAction tradeType, String userUid, String stockName, Float stockAmount) {
        this.tradeType = tradeType;
        this.userUid = userUid;
        this.stockName = stockName;
        this.stockAmount = stockAmount;
    }

    public static Trade fromString(String str) throws Exception {
        String[] tempArr = str.split(",");
        float amount, price;
        if (tempArr.length == 4) {
            amount = Float.parseFloat(tempArr[3]);
            return new Trade(TradeAction.valueOf(tempArr[0]), tempArr[1], tempArr[2], amount);
        } else if (tempArr.length == 5) {
            amount = Float.parseFloat(tempArr[3]);
            price = Float.parseFloat(tempArr[4]);
            return new Trade(TradeAction.valueOf(tempArr[0]), tempArr[1], tempArr[2], amount, price);
        } else {
            throw new Exception("Bad trade: Can't make trade out of \"" + str + '"');
        }
    }
}
