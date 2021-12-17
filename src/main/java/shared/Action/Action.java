package shared.Action;

public class Action {
    public ActionType actionType = null;
    public String payload = null;

    public Action(ActionType actionType, String payload) {
        this.actionType = actionType;
        this.payload = payload;
    }

    public Action() {

    }

    public static Integer generateKey() {
        return key++;
    }

    private static Integer key = 0;
}
