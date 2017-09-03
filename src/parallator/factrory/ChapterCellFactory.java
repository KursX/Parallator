package parallator.factrory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.util.Callback;
import parallator.Chapter;
import parallator.controller.MainController;

import java.util.Optional;
import java.util.stream.Collectors;

public class ChapterCellFactory implements Callback<TableColumn<Chapter, String>, TableCell<Chapter, String>> {

    private MainController controller;

    public ChapterCellFactory(MainController controller, TableView<Chapter> chapters) {
        ObservableList<Chapter> lines = FXCollections.observableArrayList();
        this.controller = controller;
        lines.addAll(controller.getFilesList().stream().map(s -> new Chapter(s.getName())).collect(Collectors.toList()));
        chapters.setItems(lines);
        controller.showChapter(controller.getConfig().getLastChapter());
    }

    @Override
    public TableCell<Chapter, String> call(final TableColumn<Chapter, String> param) {
        TableCell<Chapter, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        cell.setOnMouseClicked(t -> {
            if (t.getButton() == MouseButton.PRIMARY) {
                if (controller.isEdited()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Сохранить изменения?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        controller.save();
                    }
                    if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                        return;
                    }
                }
                controller.showChapter(cell.getIndex());
                controller.getConfig().setLastChapter(cell.getIndex());
            }
        });
        return cell;
    }
}