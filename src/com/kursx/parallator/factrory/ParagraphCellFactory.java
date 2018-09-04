package com.kursx.parallator.factrory;

import com.kursx.parallator.Logger;
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
import com.kursx.parallator.MainConfig;
import com.kursx.parallator.controller.EditDialogController;
import com.kursx.parallator.controller.MainController;

import java.io.IOException;
import java.util.*;

public class ParagraphCellFactory implements Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>> {

    private MainController controller;
    private String key;
    private ContextMenu contextMenu;

    public ParagraphCellFactory(MainController controller, String key) {
        this.controller = controller;
        this.key = key;
    }

    public static boolean notValid(String data, int parts) {
        return data.length() > 450 || (data.length() > 300 && parts > 5);
    }

    public TableCell<Map<String, String>, String> call(final TableColumn<Map<String, String>, String> param) {
        TableCell<Map<String, String>, String> cell = new TableCell<Map<String, String>, String>() {
            protected void updateItem(String data, boolean empty) {
                super.updateItem(data, empty);
                if (!empty && data != null) {
                    if (notValid(data, getParts(data).size()) && controller.getConfig().isRed()) {
                        ((Text) getGraphic()).setFill(Color.RED);
                    } else {
                        ((Text) getGraphic()).setFill(Color.BLACK);
                    }
                }
            }
        };

        if (!(cell.itemProperty() + "").equals("null")) {
            Text text = new Text();
            text.setFont(new Font(MainConfig.getMainConfig().getFontSize()));
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(cell.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            cell.setMinHeight(cell.itemProperty().toString().length() * 60 / 100);
            cell.setGraphic(text);
        } else {
            Text text = new Text();
            text.setFont(new Font(MainConfig.getMainConfig().getFontSize()));
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(cell.widthProperty());
            text.textProperty().setValue("Картинка");
            cell.setGraphic(text);
        }
        cell.setOnMouseClicked(t -> {
            controller.getConfig().addBookmark(cell.getIndex(), controller.getFile());


            final String data = param.getCellObservableValue(cell.getIndex()).getValue();
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
                    MenuItem auto = new MenuItem("Авторазбиение");
                    contextMenu.getItems().addAll(auto, new SeparatorMenuItem());
                    List<String> separators = new ArrayList<>();
                    for (int i = 1; i < parts.size(); i++) {
                        final int index = i;
                        String left = parts.get(i - 1).length() > 14 ? parts.get(i - 1).substring(
                                parts.get(i - 1).length() - 15) : parts.get(i - 1);
                        String separator = dividers.get(i - 1);
                        separators.add(separator);
                        String right = parts.get(i).length() > 14 ? parts.get(i).substring(0, 15) : parts.get(i);
                        MenuItem item = new MenuItem(left + "<- " + separator + " ->" + right);
                        item.setOnAction(event -> controller.separate(cell.getIndex(), key, parts, index, dividers));
                        contextMenu.getItems().add(item);
                    }
                    auto.setOnAction(event -> controller.separate(cell.getIndex(), key, data, dividers));
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
                    new Thread(() -> controller.nextChapter(cell.getIndex(), key)).start();
                });
                delete.setOnAction(event -> {
                    controller.remove(cell.getIndex(), key);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_edit.fxml"));
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
            Logger.exception(e);
        }
    }

    public static List<String> getParts(String data) {
        String dividers = "(";
        for (String divider : MainConfig.getMainConfig().dividers) {
            dividers += divider + "|";
        }
        dividers = dividers.substring(0, dividers.length() - 1) + ")";
        List<String> parts = new ArrayList<>(Arrays.asList(data.split(dividers)));
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, parts.get(i).trim());
        }
        new ArrayList<>(parts).stream().filter(part -> part.trim().length() < 2).forEach(parts::remove);
        if (parts.size() > 1) {
            int lio = data.lastIndexOf(parts.get(parts.size() - 1));
            parts.set(parts.size() - 1, parts.get(parts.size() - 1) + data.substring(lio + parts.get(parts.size() - 1).length()));
        }
        return parts;
    }
}