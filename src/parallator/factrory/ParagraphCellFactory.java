package parallator.factrory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
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
    private ContextMenu contextMenu;
    private static final String dividersRegex = "(\\.”|\\.\"|\\.|!|\\?|\\.{3}|…)";

    public ParagraphCellFactory(MainController controller, boolean left) {
        this.controller = controller;
        this.en = left;
    }

    public TableCell<Paragraph, String> call(final TableColumn<Paragraph, String> param) {
        TableCell<Paragraph, String> cell = new TableCell<Paragraph, String>() {
            protected void updateItem(String data, boolean empty) {
                super.updateItem(data, empty);
                if (!empty) {
                    if (getParts(data).size() > 5 && controller.getConfig().isRed()) {
                        ((Text) getGraphic()).setFill(Color.RED);
                    } else {
                        ((Text) getGraphic()).setFill(Color.BLACK);
                    }
                }
            }
        };

        Text text = new Text();

        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        cell.setMinHeight(cell.itemProperty().toString().length() * 60 / 100);


        cell.setGraphic(text);
        cell.setOnMouseClicked(t -> {
            String data = param.getCellObservableValue(cell.getIndex()).getValue();
            List<String> parts = getParts(data);
            new ArrayList<>(parts).stream().filter(part -> part.trim().length() < 2).forEach(parts::remove);
            if (contextMenu != null) contextMenu.hide();
            contextMenu = new ContextMenu();
            if (t.getButton() == MouseButton.PRIMARY) {
                MenuItem edit = new MenuItem("Редактировать");
                edit.setOnAction(event -> showEdit(data, cell.getIndex()));
                List<String> dividers = new ArrayList<>();
                int position = 0;

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
                } else {
                    showEdit(data, cell.getIndex());
                    return;
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

    private void showEdit(String data, int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_edit.fxml"));
            Parent root = loader.load();
            Scene dialogScene = new Scene(root);
            Stage dialogStage = new Stage();
            dialogStage.setScene(dialogScene);
            EditDialogController dialogController = loader.getController();
            dialogController.init(dialogStage, () -> {
                controller.update(index, en, dialogController.getText());
                dialogController.close();
            });
            dialogController.setText(data);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getParts(String data) {
        List<String> parts = new ArrayList<>(Arrays.asList(data.split(dividersRegex)));
        new ArrayList<>(parts).stream().filter(part -> part.trim().length() < 2).forEach(parts::remove);
        return parts;
    }
}
