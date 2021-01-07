package com.kursx.parallator.controller;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.kursx.parallator.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class BookDialogController implements Initializable {

    private Stage stage;
    private Main main;
    private List<Chapter> chapters;
    private final String thumbnailName = "thumbnail.jpg";
    public static final ObservableList<String> langs = FXCollections.observableArrayList(
            "ru", "en", "be", "bg", "cs", "da", "de", "el", "es", "et", "fi", "fr",
            "it", "lt", "lv", "nl", "no", "pl", "pt", "sk", "sv", "tr", "tt", "uk", "ro"
    );

    public void init(Stage stage, Main main) {
        this.stage = stage;
        this.main = main;

        File thumbnailImage = new File(main.getRootController().getFile(), thumbnailName);
        if (thumbnailImage.exists()) {
            thumbnail.setVisible(true);
            thumbnail.setImage(new Image(thumbnailImage.toURI().toString()));
        }

        stage.setOnShowing(event -> {
            Book book = main.getRootController().getBook();
            if (book != null) {
                hash.setText(book.hash);
                from.getSelectionModel().select(book.getLang().split("-")[0]);
                to.getSelectionModel().select(book.getLang().split("-")[1]);
                fromField.setText(book.getName());
                fromAuthor.setText(book.getAuthor());
                thumbnail.setVisible(true);
                thumbnail.setImage(new Image(new File(main.getRootController().getFile(), book.getThumbnail()).toURI().toString()));

                for (Book.Lang lang : book.getLangs()) {
                     for (LangItem field : langItems) {
                        if (field.getLang().lang.equals(lang.lang)) {
                            field.setText(field, lang);
                            break;
                        }
                    }
                }
            }
        });
        chapters = main.getRootController().validate();
        if (chapters == null) {
            Toast.makeText(stage, "Не во всех главах количество русских абзацев совпадает с количеством английских");
        }
    }

    @FXML
    ImageView thumbnail;

    @FXML
    TextField fromField, fromAuthor, hash;

    @FXML
    Button cancel, save, thumbnailButton, addLang;

    @FXML
    ComboBox<String> from, to;

    @FXML
    VBox langsBox;

    private TextField[] fields;
    private List<LangItem> langItems = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        langsBox.setSpacing(5);
        String[] initialLangs = new String[]{
                "en", "ru", "de", "uk", "bg", "it", "es", "fr", "tr"
        };
        for (String initialLang : initialLangs) {
            LangItem langItem = new LangItem(initialLang);
            langItem.add(langsBox);
            langItems.add(langItem);
        }

        save.setOnAction(event -> {
            save();
            stage.close();
        });
        addLang.setOnAction(event -> {
            LangItem langItem = new LangItem(from.getSelectionModel().getSelectedItem());
            langItem.add(langsBox);
            langItems.add(langItem);
        });
        cancel.setOnAction(event -> stage.close());
        fields = new TextField[] {
                fromField, fromAuthor
        };
        thumbnailButton.setOnAction(event -> {
            File file = Helper.showFileChooser(stage.getScene(), new FileChooser.ExtensionFilter("jpg", "*.jpg"));
            if (file != null) try {
                ImageHelper.copyFile(file, new File(main.getRootController().getFile(), thumbnailName));
                Image image = new Image(file.toURI().toString());
                if (!ImageHelper.checkImageSize(image)) {
                    Toast.makeText(stage, "Загрузите квадратную картинку");
                }
                thumbnail.setVisible(true);
                thumbnail.setImage(image);
            } catch (Exception e) {
                Logger.exception(e);
            }
        });

        from.setItems(langs);
        to.setItems(langs);
        from.getSelectionModel().select("en");
        to.getSelectionModel().select("ru");
        if (!Helper.isDebug()) {
            hash.setVisible(false);
        }
    }

    public boolean save() {
        try {
            saveBookToJsonFile(main.getRootController());
            BookConverter.saveDirectoriesToJsonFile(main.getRootController());
        } catch (Exception e) {
            Logger.exception(e);
            Toast.makeText(stage, "Произошла ошибка");
            return false;
        }
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                Toast.makeText(stage, "Не все поля заполнены");
                return false;
            }
        }
        for (LangItem field : langItems) {
            if ((field.getLang().lang.equals(from.getSelectionModel().getSelectedItem())
                    || field.getLang().lang.equals(to.getSelectionModel().getSelectedItem())) && !field.isFilled()) {
                Toast.makeText(stage, "Не все поля заполнены");
                return false;
            }
        }
        return true;
    }

    private void saveBookToJsonFile(MainController controller) throws FileNotFoundException {
        Book book = controller.getBook();
        String hashString = hash.getText().isEmpty() && !Helper.isDebug() ?
                (new Date().getTime() + "00") : hash.getText();
        if (book == null) {
            book = new Book();
        }
        book.update(hashString, fromField.getText(), fromAuthor.getText(),
                from.getSelectionModel().getSelectedItem(), to.getSelectionModel().getSelectedItem(), thumbnailName);
        book.getLangs().clear();
        for (LangItem langItem : langItems) {
            book.getLangs().add(langItem.getLang());
        }
        book.setChapters(null);
        PrintWriter printWriter = new PrintWriter(new File(controller.getFile(), "book.json"));
        printWriter.print(new Gson().toJson(book));
        printWriter.flush();
        printWriter.close();
    }
}
