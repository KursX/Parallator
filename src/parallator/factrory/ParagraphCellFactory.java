package parallator.factrory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import parallator.Paragraph;
import parallator.controller.EditDialogController;
import parallator.controller.MainController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ParagraphCellFactory implements Callback<TableColumn<Paragraph, String>, TableCell<Paragraph, String>> {

    private MainController controller;
    private boolean en;
    private final String dividersRegex = "(\\.”|\\.\"|\\.|!|\\?|\\.{3}|…)";

    public ParagraphCellFactory(MainController controller, boolean left) {
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
            String data = param.getCellObservableValue(cell.getIndex()).getValue();
            ContextMenu contextMenu = new ContextMenu();
            if (t.getButton() == MouseButton.PRIMARY) {
                MenuItem edit = new MenuItem("Редактировать");
                edit.setOnAction(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/edit_dialog.fxml"));
                        Parent root = loader.load();
                        Scene dialogScene = new Scene(root);
                        Stage dialogStage = new Stage();
                        dialogStage.setScene(dialogScene);
                        EditDialogController dialogController = loader.getController();
                        dialogController.init(dialogStage, controller, en, cell.getIndex());
                        dialogController.setText(data);
                        dialogStage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                List<String> dividers = new ArrayList<>();
                int position = 0;
                List<String> parts = new ArrayList<>(Arrays.asList(data.split(dividersRegex)));

                new ArrayList<>(parts).stream().filter(part -> part.trim().length() < 2).forEach(parts::remove);

                for (int index = 1; index < parts.size(); index++) {
                    position += parts.get(index - 1).length();
                    String divider = data.substring(position, data.indexOf(parts.get(index)));
                    dividers.add(divider);
                    position += divider.length();
                }

                if (parts.size() > 1) {
                    for (int i = 1; i < parts.size(); i++) {
                        final int index = i;
                        String left = parts.get(i - 1).length() > 14 ? parts.get(i - 1).substring(
                                parts.get(i - 1).length() - 15) : parts.get(i - 1);
                        String separator = dividers.get(i - 1);
                        String right = parts.get(i).length() > 14 ? parts.get(i).substring(0, 15) : parts.get(i);
                        MenuItem item = new MenuItem(left + "<- " + separator + " ->" + right);
                        item.setOnAction(event -> controller.separate(cell.getIndex(), en, parts, index, separator));
                        contextMenu.getItems().add(item);
                    }
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }

                contextMenu.getItems().add(edit);
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
