package client;

import client.jfx.App;
import javafx.application.Application;
import shared.Const;

public class RunClient {
    public static void main(String[] args) {
        Thread jfxThread = new Thread(() -> Application.launch(App.class));
        jfxThread.start();
        App myApp = App.waitForApp();

        ClientActionsManager clientActionsManager = ClientActionsManager.getInstance(Const.defaultUser.uid);
        clientActionsManager.run();
    }
}
