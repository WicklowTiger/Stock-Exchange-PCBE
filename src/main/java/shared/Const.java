package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for storing constant values shared by the app's modules
 */
public final class Const {
    private Const() {}

    public static final ArrayList<String> clientListenTopics = new ArrayList<String>(Arrays.asList("tradeReplies", "stockUpdates", "userUpdates"));
    public static final ArrayList<String> serverListenTopics = new ArrayList<String>(Arrays.asList("tradeMessages", "keepAlive"));
    public static final String bootstrapServerIP = "127.0.0.1:9092";
    public static final User defaultUser = new User("03u21809u7y3", "Test User", 1500f);
    public static final User defaultUser2 = new User("48371ndj9853", "Test User 2", 3500f);
    public static final Integer keepAliveThreshold = 30;

}
