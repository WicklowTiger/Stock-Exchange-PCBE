package shared;

public enum Topic {
    STOCK_UPDATES,
    TRADE_MESSAGES,
    KEEP_ALIVE,
    TRADE_REPLIES,
    USER_UPDATES,
    NONE;

    public static String toString(Topic topic) {
        switch (topic) {
            case TRADE_REPLIES:
                return "tradeReplies";
            case STOCK_UPDATES:
                return "stockUpdates";
            case KEEP_ALIVE:
                return "keepAlive";
            case USER_UPDATES:
                return "userUpdates";
            case TRADE_MESSAGES:
                return "tradeMessages";
            default:
                return "none";
        }
    }

    public static Topic fromString(String str) {
        switch (str) {
            case "tradeReplies":
                return Topic.TRADE_REPLIES;
            case "stockUpdates":
                return Topic.STOCK_UPDATES;
            case "keepAlive":
                return Topic.KEEP_ALIVE;
            case "userUpdates":
                return Topic.USER_UPDATES;
            case "tradeMessages":
                return Topic.TRADE_MESSAGES;
            default:
                return Topic.NONE;
        }
    }
}
