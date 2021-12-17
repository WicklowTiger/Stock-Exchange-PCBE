package client.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.kafka.common.serialization.StringSerializer;
import server.ServerProducer;
import shared.MessageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Scene scene;

    public static final CountDownLatch latch = new CountDownLatch(1);
    public static App inst = null;

    public static App waitForApp() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inst;
    }

    public static void setApp(App app) {
        inst = app;
        latch.countDown();
    }

    public App() {
        setApp(this);
    }

    public void printSomething() {
        System.out.println("You called a method on the application");
    }


    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("HomeWindow"), 850, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File("src/main/resources/jfx/" + fxml + ".fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader();
        return fxmlLoader.load(fileInputStream);
    }

    public static void main(String[] args) {
        launch();
    }

}
