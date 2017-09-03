package parallator;

import com.kursx.parser.fb2.Section;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private MainController rootController;

    @Override
    public void start(Stage rootStage) throws Exception {
        MainConfig mainConfig = MainConfig.getMainConfig();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/main.fxml"));
        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/resources/layouts/book_dialog.fxml"));

        Parent root = loader.load();
        Parent dialogRoot = dialogLoader.load();

        Scene dialogScene = new Scene(dialogRoot);
        Scene rootScene = new Scene(root);

        rootStage.setTitle("Parallator");

        rootController = loader.getController();
        DialogController dialogController = dialogLoader.getController();

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

        info.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Отправка");
            dialogStage.setScene(dialogScene);

            dialogController.init(dialogStage, this);
            dialogStage.show();
            Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
        });
        about.setOnAction(event -> Toast.makeText(rootStage, "Parallator v0.6 by KursX \n kursxinc@gmail.com", 5000));
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
        book.getItems().addAll(info);

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
        if (mainConfig.path() != null) rootController.open(new File(mainConfig.path()));
    }

    public MainController getRootController() {
        return rootController;
    }



    public static void main(String[] args) {
//            try {
////                FictionBook fictionBook1 = new FictionBook(new File("src/1.fb2"));
//                FictionBook fictionBook2 = new FictionBook(new File("src/2.fb2"));
////                List<Section> sections1 = new ArrayList<>();
//                List<Section> sections2 = new ArrayList<>();
////                process(fictionBook1.getBody().getSections(), sections1);
//                process(fictionBook2.getBody().getSections(), sections2);
//
////                sections1.remove(0);
//
//
//                sections2.add(new Section());
////                write(sections1, "/1.txt");
//                write(sections2, "/2.txt");
//
//            } catch (ParserConfigurationException | IOException | SAXException e) {
//                e.printStackTrace();
//            }
        launch(args);
        }

        public static void write(List<Section> sections, String name) throws IOException {
            for (int i = 0; i < sections.size(); i++) {
                StringBuilder stringBuilder = new StringBuilder();
                sections.get(i).getParagraphs().stream().filter(p -> p.getP() != null).forEach(p -> stringBuilder.append(p.getP()).append("\n\n"));
                new File("src/book/" + (i + 1)).mkdirs();
                FileWriter fileWriter = new FileWriter(new File("src/book/" + (i + 1) + name));
                fileWriter.append(stringBuilder);
                fileWriter.flush();
                fileWriter.close();
            }
        }

        public static void process(List<Section> bookSections, List<Section> allSections) {
            for (Section section : bookSections) {
                if (section.getSections().isEmpty()) {
                    if (!section.getParagraphs().isEmpty()) allSections.add(section);
                } else {
                    process(section.getSections(), allSections);
                }
            }
        }
}
