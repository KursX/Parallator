package parallator.factrory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import parallator.MainConfig;
import parallator.controller.EditDialogController;
import parallator.controller.MainController;

import java.io.IOException;
import java.util.*;

public class ParagraphCellFactory implements Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>> {

    private MainController controller;
    private String key;
    private ContextMenu contextMenu;
    private static final String dividersRegex = "(\\.”|\\.\"|\\.|!|\\?|\\.{3}|…)";

    public ParagraphCellFactory(MainController controller, String key) {
        this.controller = controller;
        this.key = key;
    }

    public TableCell<Map<String, String>, String> call(final TableColumn<Map<String, String>, String> param) {
        TableCell<Map<String, String>, String> cell = new TableCell<Map<String, String>, String>() {
            protected void updateItem(String data, boolean empty) {
                super.updateItem(data, empty);
                if (!empty && data != null) {
                    if ((data.length() > 500 || getParts(data).size() > 5) && controller.getConfig().isRed()) {
                        ((Text) getGraphic()).setFill(Color.RED);
                    } else {
                        ((Text) getGraphic()).setFill(Color.BLACK);
                    }
                }
            }
        };

        Text text = new Text();
        text.setFont(new Font(MainConfig.getMainConfig().getFontSize()));
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
                int start = 0;

                for (int index = 1; index < parts.size(); index++) {
                    start += parts.get(index - 1).length();
                    int end = data.indexOf(parts.get(index), start);
                    String divider = data.substring(start, end);
                    dividers.add(divider);
                    start += divider.length();
                }

                if (parts.size() > 1) {
                    for (int i = 1; i < parts.size(); i++) {
                        final int index = i;
                        String left = parts.get(i - 1).length() > 14 ? parts.get(i - 1).substring(
                                parts.get(i - 1).length() - 15) : parts.get(i - 1);
                        String separator = dividers.get(i - 1);
                        String right = parts.get(i).length() > 14 ? parts.get(i).substring(0, 15) : parts.get(i);
                        MenuItem item = new MenuItem(left + "<- " + separator + " ->" + right);
                        item.setOnAction(event -> controller.separate(cell.getIndex(), key, parts, index, separator));
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
                MenuItem nextChapter = new MenuItem("Переместить в следующую главу");
                contextMenu.getItems().addAll(up, delete, down, nextChapter);
                nextChapter.setOnAction(event -> {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            controller.nextChapter(cell.getIndex(), key);
                        }
                    }).start();
                });
                delete.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Удалить абзац?");
                    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        controller.remove(cell.getIndex(), key);
                    }
                });
                up.setOnAction(event -> {
                    controller.down(cell.getIndex(), key);
                });
                down.setOnAction(event -> {
                    controller.up(cell.getIndex(), key);
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
                controller.update(index, key, dialogController.getText());
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
