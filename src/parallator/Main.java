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
        Menu enc1 = new Menu("Кодировка исходника");
        Menu enc2 = new Menu("Кодировка перевода");

        MenuItem open = new MenuItem("Открыть");
        MenuItem save = new MenuItem("Сохранить");
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить программу");
        MenuItem send = new MenuItem("Отправить KursX");
        MenuItem refresh = new MenuItem("Обновить текст");

        for (String charset : Helper.charsets) {
            MenuItem e1 = new MenuItem(charset);
            MenuItem e2 = new MenuItem(charset);
            enc1.getItems().add(e1);
            enc2.getItems().add(e2);
            e1.setOnAction(event -> {
                Config config = Helper.getConfig(rootController.getFile());
                config.enc1 = e1.getText();
                Helper.saveConfig(config, rootController.getFile());
                rootController.change();
            });
            e2.setOnAction(event -> {
                Config config = Helper.getConfig(rootController.getFile());
                config.enc2 = e2.getText();
                Helper.saveConfig(config, rootController.getFile());
                rootController.change();
            });
        }

        send.setOnAction(event -> {
            if (rootController.getFile() != null) {
                dialogStage.show();
                Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
            }
        });
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.3 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> Helper.update());
        open.setOnAction(event -> rootController.open());
        save.setOnAction(event -> rootController.save());
        refresh.setOnAction(event -> rootController.change());

        file.getItems().addAll(open, save, send, enc1, enc2, refresh);
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
