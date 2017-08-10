package parallator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parallator.controller.DialogController;
import parallator.controller.RootController;

public class Main extends Application {

    private RootController rootController;
    private DialogController dialogController;

    @Override
    public void start(Stage rootStage) throws Exception {
        Stage dialogStage = new Stage();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("layouts/main.fxml"));
        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("layouts/book_dialog.fxml"));

        Parent root = loader.load();
        Parent dialogRoot = dialogLoader.load();

        Scene dialogScene = new Scene(dialogRoot);
        Scene rootScene = new Scene(root);

        rootStage.setTitle("Parallator");
        dialogStage.setTitle("Отправка");

        rootController = loader.getController();
        dialogController = dialogLoader.getController();

        rootStage.setScene(rootScene);
        dialogStage.setScene(dialogScene);

        rootController.setStage(rootStage);
        dialogController.init(dialogStage, this);


        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        Menu help = new Menu("Помощь");

        MenuItem open = new MenuItem("Открыть");
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить");
        MenuItem send = new MenuItem("Отправить KursX");

        send.setOnAction(event -> {
            if (rootController.getFile() != null) {
                dialogStage.show();
                Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
            }
        });
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.2 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> Helper.update());
        open.setOnAction(event -> rootController.open());

        file.getItems().addAll(open, send);
        help.getItems().addAll(update, about);

        menuBar.getMenus().addAll(file, help);

        ((VBox) rootScene.getRoot()).getChildren().add(0, menuBar);

        rootStage.setMaximized(true);
        rootStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public RootController getRootController() {
        return rootController;
    }

    public DialogController getDialogController() {
        return dialogController;
    }
}
