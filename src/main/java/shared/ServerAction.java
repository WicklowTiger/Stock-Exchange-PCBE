package shared;

public class ServerAction {
    public ServerActionType actionType = null;
    public String payload = null;

    public ServerAction(ServerActionType actionType, String payload) {
        this.actionType = actionType;
        this.payload = payload;
    }

    public ServerAction() {

    }

    public static Integer generateKey() {
        return key++;
    }

    private static Integer key = 0;
}

