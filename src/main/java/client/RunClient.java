package client;

import client.jfx.App;
import client.jfx.HomeWindowController;
import javafx.application.Application;
import shared.Action;
import shared.ActionType;
import shared.Stock;

import java.util.concurrent.TimeUnit;

public class RunClient {
    public static void main(String[] args) {
//
        Thread jfxThread = new Thread(() -> Application.launch(App.class));
        jfxThread.start();
        App myApp = App.waitForApp();

        ClientActionsManager clientActionsManager = ClientActionsManager.getInstance();
        clientActionsManager.run();
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClientActionsManager.putAction(new Action(ActionType.SEND_BUY, "AAPL,0.5,34.34"));
        ClientActionsManager.putAction(new Action(ActionType.SEND_BUY, "AAPL,0.5"));
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ClientActionsManager.putAction(new Action(ActionType.SEND_SELL, "AAPL,1.7"));
    }
}
