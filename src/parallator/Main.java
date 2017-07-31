package parallator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Parallator");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
        Controller controller = loader.getController();
        controller.setScene(primaryStage.getScene());
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        MenuItem save = new MenuItem("Сохранить");
        save.setOnAction(event -> controller.save());
        menuBar.getMenus().addAll(file);
        file.getItems().add(save);
        ((VBox) primaryStage.getScene().getRoot()).getChildren().add(0, menuBar);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
