package client.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("HomeWindow"), 850, 600);
        stage.setScene(scene);
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
