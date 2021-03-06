import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainChat extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 420, 500 );
        stage.setTitle("Chat!");
        stage.setScene(scene);
        stage.show();
    }
}
