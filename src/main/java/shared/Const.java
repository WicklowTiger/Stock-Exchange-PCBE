package shared;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for storing constant values shared by the app's modules
 */
public final class Const {
    private Const() {}

    public static final String bootstrapServerIP = "127.0.0.1:9092";
    public static final Map<String, String> topicMap = new HashMap<String, String>() {{
        put("tradeTopic", "heartbeatTopic");
    }};
}
