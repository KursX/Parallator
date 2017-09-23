package parallator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parallator.controller.BookDialogController;
import parallator.controller.Fb2DialogController;
import parallator.controller.MainController;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

    private MainController rootController;

    @Override
    public void start(Stage rootStage) throws Exception {
        MainConfig mainConfig = MainConfig.getMainConfig();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/main.fxml"));
        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_book.fxml"));

        Parent root = loader.load();
        Parent dialogRoot = dialogLoader.load();

        Scene dialogScene = new Scene(dialogRoot);
        Scene rootScene = new Scene(root);

        rootStage.setTitle("Parallator");

        rootController = loader.getController();
        rootController.setStage(rootStage);
        BookDialogController dialogController = dialogLoader.getController();

        rootStage.setScene(rootScene);

        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        Menu book = new Menu("Книга");
        Menu help = new Menu("Помощь");
        Menu enc1 = new Menu("Кодировка исходника");
        Menu enc2 = new Menu("Кодировка перевода");
        Menu divider = new Menu("Разделитель абзацев");
        Menu last = new Menu("Последние книги");

        MenuItem info = new MenuItem("Описание");
        MenuItem fb2 = new MenuItem("Импорт из Fb2");
        MenuItem open = new MenuItem("Открыть");
        open.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        MenuItem save = new MenuItem("Сохранить");
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить программу");
        MenuItem refresh = new MenuItem("Обновить текст");
        refresh.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));

        mainConfig.getPathes().stream().filter(path -> new File(path).exists()).forEach(path -> {
            MenuItem item = new MenuItem(path);
            last.getItems().add(item);
            item.setOnAction(event -> {
                mainConfig.setBookPath(path);
                rootController.open(new File(path));
            });
        });

        for (String charset : Helper.charsets) {
            MenuItem e1 = new MenuItem(charset);
            MenuItem e2 = new MenuItem(charset);
            enc1.getItems().add(e1);
            enc2.getItems().add(e2);
            e1.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setEnc1(e1.getText());
                rootController.showChapter();
            });
            e2.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setEnc2(e2.getText());
                rootController.showChapter();
            });
        }

        for (int i = 0; i < Helper.dividers.length; i++) {
            MenuItem item = new MenuItem(Helper.dividers[i]);
            divider.getItems().add(item);
            final int index = i;
            item.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setDivider(Helper.dividersRegs[index]);
                rootController.showChapter();
            });
        }

        fb2.setOnAction(event -> {
            try {
                FXMLLoader fb2Loader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_fb2.fxml"));
                Parent parent = fb2Loader.load();
                Scene scene = new Scene(parent);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
                Fb2DialogController controller = fb2Loader.getController();
                controller.init(stage, rootController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        info.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Отправка");
            dialogStage.setScene(dialogScene);

            dialogController.init(dialogStage, this);
            dialogStage.show();
            Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
        });
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.7 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> Helper.update());
        open.setOnAction(event -> {
            File dir = Helper.showDirectoryChooser(rootScene);
            if (dir != null) {
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
            rootController.showChapter();
        });

        file.getItems().addAll(open, save, last, enc1, enc2, divider, refresh);
        help.getItems().addAll(update, about);
        book.getItems().addAll(info, fb2);

        menuBar.getMenus().addAll(file, help, book);


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

        rootScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DOWN:
                    rootController.down();
                    break;
                case UP:
                    rootController.up();
                    break;
            }
        });

        if (mainConfig.path() != null) rootController.open(new File(mainConfig.path()));
    }

    public MainController getRootController() {
        return rootController;
    }



    public static void main(String[] args) {
        launch(args);
    }
}
