package shared;

public class MessageOptions<T> {
    T key = null;
    String partition = null;

    public MessageOptions(T key, String partition) {
        this.key = key;
        this.partition = partition;
    }
}
