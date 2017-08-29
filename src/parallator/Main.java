package parallator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parallator.controller.DialogController;
import parallator.controller.MainController;

import java.io.File;
import java.util.Optional;

public class Main extends Application {

    private MainController rootController;

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
        DialogController dialogController = dialogLoader.getController();

        rootStage.setScene(rootScene);
        dialogStage.setScene(dialogScene);

        dialogController.init(dialogStage, this);

        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        Menu help = new Menu("Помощь");
        Menu enc1 = new Menu("Кодировка исходника");
        Menu enc2 = new Menu("Кодировка перевода");
        Menu divider = new Menu("Разделитель абзацев");

        MenuItem open = new MenuItem("Открыть");
        open.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        MenuItem save = new MenuItem("Сохранить");
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить программу");
        MenuItem send = new MenuItem("Отправить KursX");
        MenuItem refresh = new MenuItem("Обновить текст");
        refresh.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));

        for (String charset : Helper.charsets) {
            MenuItem e1 = new MenuItem(charset);
            MenuItem e2 = new MenuItem(charset);
            enc1.getItems().add(e1);
            enc2.getItems().add(e2);
            e1.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Helper.getConfig(rootController.getFile()).setEnc1(e1.getText());
                rootController.change();
            });
            e2.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Helper.getConfig(rootController.getFile()).setEnc2(e2.getText());
                rootController.change();
            });
        }

        for (int i = 0; i < Helper.dividers.length; i++) {
            MenuItem item = new MenuItem(Helper.dividers[i]);
            divider.getItems().add(item);
            final int index = i;
            item.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Helper.getConfig(rootController.getFile()).setDivider(Helper.dividersRegs[index]);
                rootController.change();
            });
        }

        send.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            dialogStage.show();
            Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
        });
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.5 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> Helper.update());
        open.setOnAction(event -> {
            File dir = Helper.showDirectoryChooser(rootScene);
            if (dir != null) {
                MainConfig mainConfig = Helper.getMainConfig();
                mainConfig.setBookPath(dir.getAbsolutePath());
                rootController.open(dir);
            }
        });
        save.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            rootController.save();
        });
        refresh.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            rootController.change();
        });

        file.getItems().addAll(open, save, enc1, enc2, divider, refresh, send);
        help.getItems().addAll(update, about);

        menuBar.getMenus().addAll(file, help);

        ((VBox) rootScene.getRoot()).getChildren().add(0, menuBar);

        rootStage.setMaximized(true);
        rootStage.show();
        rootStage.setOnCloseRequest(event -> {
            if (rootController.getFile() == null || !rootController.isEdited()) return;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Сохранить перед выходом?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                rootController.save();
            }
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
            }
        });
        MainConfig mainConfig = Helper.getMainConfig();
        if (mainConfig.path() != null) rootController.open(new File(mainConfig.path()));
    }

    public static void main(String[] args) {
        launch(args);
    }

    public MainController getRootController() {
        return rootController;
    }
}
