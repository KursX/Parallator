package parallator.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import parallator.Book;

public class LangItem {

    private HBox box = new HBox();
    private ComboBox<String> lang = new ComboBox<>();
    public TextField author = new TextField();
    public TextField name = new TextField();

    public LangItem(String from) {
        box.setSpacing(5);
        ObservableList<String> langs = FXCollections.observableArrayList(BookDialogController.langs);
        langs.remove(from);
        lang.setItems(BookDialogController.langs);
        lang.getSelectionModel().select(BookDialogController.langs.indexOf(from));
        box.getChildren().addAll(lang, author, name);
    }

    public void add(VBox vBox) {
        vBox.getChildren().add(box);
    }

    public Book.Lang getLang() {
        return new Book.Lang(lang.getSelectionModel().getSelectedItem(), name.getText(), author.getText());
    }

    public boolean isFilled() {
        return !name.getText().isEmpty() && !author.getText().isEmpty();
    }
}
