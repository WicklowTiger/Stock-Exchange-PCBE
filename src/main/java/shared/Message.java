package shared;


public class Message<K, V> {
    public K key = null;
    public V value = null;
    public Topic topic = Topic.NONE;

    public Message(K key, V value, String topic) {
        this.key = key;
        this.value = value;
        switch(topic) {
            case "stockUpdates":
                this.topic = Topic.STOCK_UPDATES;
                break;
            case "tradeReplies":
                this.topic = Topic.TRADE_REPLIES;
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "key=" + key +
                ", value=" + value +
                ", topic=" + topic +
                '}';
    }
}
