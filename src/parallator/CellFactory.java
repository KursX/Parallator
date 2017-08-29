package parallator;

import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.util.Callback;
import parallator.controller.MainController;

import java.util.Optional;

public class CellFactory implements Callback<TableColumn<Paragraph, String>, TableCell<Paragraph, String>> {

    private MainController controller;
    private boolean en;

    public CellFactory(MainController controller, boolean left) {
        this.controller = controller;
        this.en = left;
    }

    @Override
    public TableCell<Paragraph, String> call(final TableColumn<Paragraph, String> param) {
        TableCell<Paragraph, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        text.wrappingWidthProperty().bind(cell.widthProperty());
        cell.setMinHeight(cell.itemProperty().toString().length() * 60 / 100);
        cell.setOnMouseClicked(t -> {
            controller.edit();
            ContextMenu contextMenu = new ContextMenu();
            if (t.getButton() == MouseButton.PRIMARY) {
                String data = param.getCellObservableValue(cell.getIndex()).getValue();
                final String[] parts = data.split("\\.");
                if (parts.length > 1) {
                    for (int i = 1; i < parts.length; i++) {
                        final int index = i;
                        String left = parts[i - 1].length() > 14 ? parts[i - 1].substring(
                                parts[i - 1].length() - 15) : parts[i - 1];
                        String right = parts[i].length() > 14 ? parts[i].substring(0, 15) : parts[i];
                        MenuItem item = new MenuItem(left + "<- . ->" + right);
                        item.setOnAction(event -> controller.separate(cell.getIndex(), en, parts, index));
                        contextMenu.getItems().add(item);
                    }
                }
            } else if (t.getButton() == MouseButton.SECONDARY) {
                MenuItem up = new MenuItem("Переместить вверх");
                MenuItem delete = new MenuItem("Удалить");
                MenuItem down = new MenuItem("Переместить вниз");
                contextMenu.getItems().addAll(up, delete, down);
                delete.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Удалить абзац?");
                    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        controller.remove(cell.getIndex(), en);
                    }
                });
                up.setOnAction(event -> {
                    controller.down(cell.getIndex(), en);
                });
                down.setOnAction(event -> {
                    controller.up(cell.getIndex(), en);
                });
            }
            contextMenu.show(cell, t.getScreenX(), t.getScreenY());
        });
        return cell;
    }
}
