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
    private Menu langs;
    private static Main main;

    public static Main getMain() {
        return main;
    }

    @Override
    public void start(Stage rootStage) throws Exception {
        rootStage.setMaximized(true);
        main = this;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/main.fxml"));

        Parent root = loader.load();

        Scene rootScene = new Scene(root);

        rootStage.setTitle("Parallator");

        rootController = loader.getController();

        rootStage.setScene(rootScene);

        initMenu(rootStage);

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

        if (MainConfig.getMainConfig().path() != null) rootController.open(new File(MainConfig.getMainConfig().path()));
    }

    public void initMenu(Stage rootStage) throws IOException {
        MainConfig mainConfig = MainConfig.getMainConfig();
        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_book.fxml"));
        Parent dialogRoot = dialogLoader.load();
        Scene dialogScene = new Scene(dialogRoot);
        BookDialogController dialogController = dialogLoader.getController();
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        Menu book = new Menu("Книга");
        Menu imp = new Menu("Импорт");
        Menu exp = new Menu("Экспорт");
        Menu view = new Menu("Вид");
        Menu font = new Menu("Размер текста");
        Menu help = new Menu("Помощь");
        Menu enc1 = new Menu("Кодировка");
        Menu divider = new Menu("Разделитель абзацев");
        Menu last = new Menu("Последние книги");
        langs = new Menu("Языки");

        MenuItem info = new MenuItem("Описание");
        MenuItem fb2 = new MenuItem("FB2");
        MenuItem csv = new MenuItem("CSV");
        MenuItem html = new MenuItem("HTML");
        MenuItem open = new MenuItem("Открыть");
        MenuItem save = new MenuItem("Сохранить");
        MenuItem undo = new MenuItem("Отменить");
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить программу");
        MenuItem refresh = new MenuItem("Обновить текст");

        open.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        undo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        refresh.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));

        undo.setOnAction(event -> rootController.undo());
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.8 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> Helper.update());
        csv.setOnAction(event -> Helper.csv(rootStage.getScene(), rootController.getTextMap(), rootController.getFile()));
        html.setOnAction(event -> Helper.html(rootStage.getScene(), rootController.getTextMap(), rootController.getFile()));

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
            enc1.getItems().add(e1);
            e1.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setEnc1(e1.getText());
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
        open.setOnAction(event -> {
            File dir = Helper.showDirectoryChooser(rootStage.getScene());
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

        for (int fontSize = 5; fontSize <= 30; fontSize++) {
            final CheckMenuItem item = new CheckMenuItem(fontSize + "");
            if (mainConfig.getFontSize() == fontSize) {
                item.setSelected(true);
            }
            final int finalValue = fontSize;
            item.setOnAction(event -> {
                ((CheckMenuItem) font.getItems().get(mainConfig.getFontSize() - 5)).setSelected(false);
                mainConfig.setFontSize(finalValue);
                item.setSelected(true);
                rootController.redraw();
            });
            font.getItems().addAll(item);
        }

        imp.getItems().addAll(fb2);
        exp.getItems().addAll(csv, html);
        file.getItems().addAll(undo, open, save, refresh, last, enc1, divider);
        help.getItems().addAll(update, about);
        book.getItems().addAll(imp, exp, info);
        view.getItems().addAll(font, langs);

        menuBar.getMenus().addAll(file, book, help, view);


        ((VBox) rootStage.getScene().getRoot()).getChildren().add(0, menuBar);
    }

    public MainController getRootController() {
        return rootController;
    }

    public Menu getLangs() {
        return langs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
