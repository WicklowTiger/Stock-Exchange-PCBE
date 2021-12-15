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

    public static final ArrayList<String> updateTopics = new ArrayList<String>(Arrays.asList("tradeReplies", "stockUpdates"));
    public static final String bootstrapServerIP = "127.0.0.1:9092";
    public static final Map<String, String> topicMap = new HashMap<String, String>() {{
        put("tradeTopic", "heartbeatTopic");
    }};
    public static final User defaultUser = new User("03u21809u7y3", "Test User", 1500f);
}
