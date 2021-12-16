package shared;

public class MessageOptions<T> {
    public T key = null;
    public String partition = null;

    public MessageOptions(T key, String partition) {
        this.key = key;
        this.partition = partition;
    }

    public MessageOptions(T key) {
        this.key = key;
    }

    public MessageOptions() {

    }
}
