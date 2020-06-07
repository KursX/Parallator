package com.kursx.parallator.menu;

import com.google.gson.Gson;
import com.kursx.parallator.*;
import com.kursx.parallator.controller.BookDialogController;
import com.kursx.parallator.controller.Fb2DialogController;
import com.kursx.parallator.controller.MainController;
import com.kursx.parallator.export.CSVExporter;
import com.kursx.parallator.export.HtmlExporter;
import com.kursx.parallator.export.OfflineExporter;
import com.kursx.parallator.export.SB2Exporter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class BookMenu {

    public final Menu menu;

    public BookMenu(MainController rootController, Stage rootStage, Main main) throws IOException {
        menu = new Menu("Книга");
        Menu imp = new Menu("Импорт");
        Menu exp = new Menu("Экспорт");
        MenuItem info = new MenuItem("Описание");
        MenuItem json = new MenuItem("JSON");
        MenuItem fb2 = new MenuItem("FB2");
        MenuItem csv = new MenuItem("CSV");
        MenuItem sb2 = new MenuItem("SB2");
        MenuItem studyEnglishWords = new MenuItem("studyenglishwords.com");
        MenuItem html = new MenuItem("HTML");
        MenuItem offline = new MenuItem("Оффлайн перевод");
        imp.getItems().addAll(fb2, json, studyEnglishWords);
        exp.getItems().addAll(csv, html, sb2, offline);
        menu.getItems().addAll(imp, exp, info);

        studyEnglishWords.setOnAction(event -> {
            final File file = Helper.showFileChooser(rootStage.getScene(),
                    new FileChooser.ExtensionFilter("HTML(en+ru)", "*.html"));
            if (file == null) return;
            final int progress = rootController.startProgress("Подождите, идет импорт html");
            new Thread(() -> {
                StudyEnglishWords.INSTANCE.parse(file);

                rootController.stopProgress(progress);
                rootController.open(file.getParentFile());
            }).start();
        });

        json.setOnAction(event -> {
            final File file = Helper.showFileChooser(rootStage.getScene(),
                    new FileChooser.ExtensionFilter("fb2", "*.json"));
            if (file == null) return;
            final int progress = rootController.startProgress("Подождите, идет импорт json");
            new Thread(() -> {
                Book book = new Gson().fromJson(Helper.getTextFromFile(file, Helper.UTF_8), Book.class);
                try {
                    Helper.write(book.getChapters(), file.getParentFile());
                } catch (IOException e) {
                    Logger.exception(e);
                }
                rootController.stopProgress(progress);
                rootController.open(file.getParentFile());
                file.renameTo(new File(file.getParentFile(), "book.json"));
            }).start();
        });

        csv.setOnAction(event -> {
            final int progress = rootController.startProgress("Подождите, идет создание offline");
            new Thread(() -> {
                BookConverter.convert(rootController, rootStage, new CSVExporter());
                rootController.stopProgress(progress);
            }).start();
        });



        offline.setOnAction(event -> {
            final int progress = rootController.startProgress("Подождите, идет создание оффлайн перевода");
            new Thread(() -> {
                BookConverter.convert(rootController, rootStage, new OfflineExporter());
                rootController.stopProgress(progress);
            }).start();
        });

        html.setOnAction(event -> {
            final int progress = rootController.startProgress("Подождите, идет создание html");
            new Thread(() -> {
                BookConverter.convert(rootController, rootStage, new HtmlExporter());
                rootController.stopProgress(progress);
            }).start();
        });


        sb2.setOnAction(event -> {
            final int progress = rootController.startProgress("Подождите, идет создание sb2");
            new Thread(() -> {
                boolean showBookInfo = !BookConverter.convert(rootController, rootStage, new SB2Exporter());
                if (showBookInfo) {
                    Platform.runLater(() -> info.getOnAction().handle(null));
                }
                rootController.stopProgress(progress);
            }).start();
        });


        fb2.setOnAction(event -> {
            try {
                FXMLLoader fb2Loader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_fb2.fxml"));
                Parent parent = fb2Loader.load();
                Scene scene = new Scene(parent);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
                Fb2DialogController controller = fb2Loader.getController();
                controller.init(stage, rootController);
            } catch (IOException e) {
                Logger.exception(e);
            }
        });

        info.setOnAction(event -> {
            try {
                FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_book.fxml"));
                Parent dialogRoot = dialogLoader.load();
                Scene dialogScene = new Scene(dialogRoot);
                BookDialogController dialogController = dialogLoader.getController();

                if (rootController.getFile() == null) return;
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Информация о книге");
                dialogStage.setScene(dialogScene);

                dialogController.init(dialogStage, main);
                dialogStage.show();
                Platform.runLater(() -> dialogStage.getScene().getRoot().requestFocus());
            } catch (IOException e) {
                Logger.exception(e);
            }
        });
    }
}
