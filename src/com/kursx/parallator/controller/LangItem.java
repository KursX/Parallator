package com.kursx.parallator.controller;

import com.kursx.parallator.MainConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.kursx.parallator.Book;

public class LangItem {

    private HBox box = new HBox();
    private ComboBox<String> lang = new ComboBox<>();
    private TextField author = new TextField();
    private TextField name = new TextField();
    private TextField translation = new TextField();
    private TextField translationSize = new TextField();

    public LangItem(String from) {
        author.setPromptText("Автор");
        name.setPromptText("Название");
        translation.setPromptText("Хэш");
        translationSize.setPromptText("Размер");
        box.setSpacing(5);
        ObservableList<String> langs = FXCollections.observableArrayList(BookDialogController.langs);
        langs.remove(from);
        lang.setItems(BookDialogController.langs);
        lang.getSelectionModel().select(BookDialogController.langs.indexOf(from));
        box.getChildren().addAll(lang, author, name);
        if (MainConfig.getMainConfig().isDeveloper()) {
            box.getChildren().addAll(translation, translationSize);
        }
    }

    public void setText(LangItem field, Book.Lang lang) {
        field.author.setText(lang.author);
        field.name.setText(lang.name);
        field.translation.setText(lang.translation);
        if (lang.translationSize != null && lang.translationSize != 0) {
            field.translationSize.setText(lang.translationSize + "");
        }
    }

    public void add(VBox vBox) {
        vBox.getChildren().add(box);
    }

    public Book.Lang getLang() {
        return new Book.Lang(lang.getSelectionModel().getSelectedItem(), name.getText(), author.getText(),
                translation.getText(), translationSize.getText());
    }

    public boolean isFilled() {
        return !name.getText().isEmpty() && !author.getText().isEmpty();
    }
}
