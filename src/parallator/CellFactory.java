package parallator;

import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.util.Callback;
import parallator.controller.RootController;

public class CellFactory implements Callback<TableColumn<Paragraph, String>, TableCell<Paragraph, String>> {

    private RootController controller;
    private boolean en;

    public CellFactory(RootController controller, boolean left) {
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
        text.setOnMouseClicked(t -> {
            if (t.getButton() == MouseButton.PRIMARY) {
                String data = param.getCellObservableValue(cell.getIndex()).getValue();
                final String[] parts = data.split("\\.");
                if (parts.length > 1) {
                    ContextMenu cm = new ContextMenu();
                    for (int i = 1; i < parts.length; i++) {
                        final int index = i;
                        String left = parts[i - 1].length() > 14 ? parts[i - 1].substring(
                                parts[i - 1].length() - 15) : parts[i - 1];
                        String right = parts[i].length() > 14 ? parts[i].substring(0, 15) : parts[i];
                        MenuItem item = new MenuItem(left + "<- . ->" + right);
                        item.setOnAction(event -> controller.separate(cell.getIndex(), en, parts, index));
                        cm.getItems().add(item);
                    }
                    cm.show(cell, t.getScreenX(), t.getScreenY());
                }
            } else if (t.getButton() == MouseButton.SECONDARY) {
                controller.remove(cell.getIndex(), en);
            }
        });
        return cell;
    }
}
