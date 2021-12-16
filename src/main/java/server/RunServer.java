package server;

public class RunServer {
    public static void main(String[] args) {
        ServerActionsManager serverActionsManager = ServerActionsManager.getInstance();
        serverActionsManager.run();
    }
}
