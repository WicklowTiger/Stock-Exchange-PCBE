package shared;


public class Message<K, V> {
    public K key = null;
    public V value = null;
    public Topic topic = Topic.NONE;

    public Message(K key, V value, String topic) {
        this.key = key;
        this.value = value;
        this.topic = Topic.fromString(topic);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public String toStringWithKey() {
        return key.toString() + "," + value.toString();
    }
}
