package com.kursx.parallator.factrory;

import com.kursx.parallator.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.kursx.parallator.Chapter;
import com.kursx.parallator.Config;
import com.kursx.parallator.Helper;
import com.kursx.parallator.controller.EditDialogController;
import com.kursx.parallator.controller.MainController;
import com.kursx.parallator.controller.ValidationException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChapterCellFactory implements Callback<TreeView<File>, TreeCell<File>> {

    private MainController controller;
    private TreeView<File> chapters;
    private File file;
    private TreeItem<File> selected;
    private File firstChapter;

    public static final String CHAPTER_REGEX = "^\\d+# .*$";

    public ChapterCellFactory(MainController controller, TreeView<File> treeView, File file) {
        this.controller = controller;
        this.chapters = treeView;
        this.file = file;
        init();
    }

    private void init() {
        TreeItem<File> rootItem = new TreeItem<>(file);
        rootItem.setExpanded(true);
        rootItem.getChildren().addAll(getItems(file));
        chapters.setRoot(rootItem);
        File lastChapter = controller.getConfig().getLastChapter(file);
        if (lastChapter != null) {
            controller.showChapter(lastChapter);
        } else if (firstChapter != null) {
            controller.showChapter(firstChapter);
        }
    }

    private ArrayList<TreeItem<File>> getItems(File file) {
        Config config = controller.getConfig();
        ArrayList<TreeItem<File>> items = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                if (!subFile.getName().contains("#")) continue;
                TreeItem<File> item;
                if (subFile.isDirectory()) {
                    item = new TreeItem<>(subFile);
                    item.getChildren().addAll(getItems(subFile));
                    items.add(item);
                    if (firstChapter == null && subFile.getName().startsWith("1#")) {
                        firstChapter = subFile;
                    }
                    if (config.getLastChapter(file) != null) {
                        if (subFile.getAbsolutePath().equals(config.getLastChapter(file).getAbsolutePath())) {
                            selected = item;
                        }
                    }
                }
            }
        }
        items.sort((o1, o2) -> {
            String first = o1.getValue().getName();
            String second = o2.getValue().getName();
            if (first.matches(CHAPTER_REGEX)) {
                first = first.split("# ")[0];
            }
            if (second.matches(CHAPTER_REGEX)) {
                second = second.split("# ")[0];
            }
            if (first.length() == second.length()) {
                return first.compareTo(second);
            } else {
                return first.length() - second.length();
            }
        });
        return items;
    }

    public void select(int index) {
        chapters.getSelectionModel().select(index);
    }

    @Override
    public TreeCell<File> call(TreeView<File> param) {
        return new TreeCell<File>() {
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText("");
                } else {
                    setText(item.getName());
                    if (isChapter(item)) {
                        setOnMouseClicked(t -> {
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
                                        int index = chapters.getRow(selected);
                                        select(index);
                                        return;
                                    }
                                }
                                controller.showChapter(item);
                                controller.getConfig().setLastChapter(item.getAbsolutePath(), file);
                            } else {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_edit.fxml"));
                                    Parent root = loader.load();
                                    Scene dialogScene = new Scene(root);
                                    Stage dialogStage = new Stage();
                                    dialogStage.setScene(dialogScene);
                                    EditDialogController dialogController = loader.getController();
                                    dialogController.init(dialogStage, () -> {
                                        String text1 = dialogController.getText();
                                        if (item.getName().matches(CHAPTER_REGEX)) {
                                            text1 = item.getName().split("#")[0] + "# " + text1;
                                        }
                                        item.renameTo(new File(item.getParentFile(), text1));
                                        init();
                                        dialogController.close();
                                    });
                                    String text = item.getName();
                                    if (text.matches(CHAPTER_REGEX)) {
                                        text = text.split("# ")[1];
                                    }
                                    dialogController.setText(text);
                                    dialogStage.show();
                                } catch (IOException e) {
                                    Logger.exception(e);
                                }
                            }
                            selected = getTreeItem();
                        });
                    }
                }
            }
        };
    }

    public static List<File> getFilesList(File file) {
        List<File> filesList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (isChapter(subFile)) {
                        filesList.add(subFile);
                    } else if (subFile.isDirectory()) {
                        filesList.addAll(getFilesList(subFile));
                    }
                }
            }
        }
        return filesList;
    }

    public static boolean isChapter(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                List<File> list = Arrays.asList(files);
                for (File subFile : list) {
                    if (subFile.getName().matches("^.*.txt$")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static ArrayList<Chapter> getChapters(File file, MainController controller) throws ValidationException {
        Config config = controller.getConfig();
        ArrayList<Chapter> items = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                if (!subFile.getName().contains("#")) continue;
                Chapter item;
                if (subFile.isDirectory()) {
                    Map<String, List<String>> paragraphs = new HashMap<>();
                    for (String key : controller.getKeys()) {
                        String en;
                        if (key.equals("en")) {
                            en = Helper.getTextFromFile(new File(subFile, key + ".txt"), config.enc1());
                        } else {
                            en = Helper.getTextFromFile(new File(subFile, key + ".txt"), config.enc1());
                        }
                        if (en != null) {
                            en = en.trim();
                            String[] ens = en.split(config.dividerRegex());
                            paragraphs.put(key, new ArrayList<>(Arrays.asList(ens)));
                        }
                    }

                    if (!paragraphs.isEmpty()) {
                        for (String key : controller.getKeys()) {
                            if (paragraphs.get(key).size() != paragraphs.get(controller.getKeys().get(0)).size())
                                throw new ValidationException();
                        }
                        List<Map<String, String>> list = new ArrayList<>();
                        for (int index = 0; index < paragraphs.get(controller.getKeys().get(0)).size(); index++) {
                            Map<String, String> map = new HashMap<>();
                            for (String key : controller.getKeys()) {
                                List<String> ps = paragraphs.get(key);
                                if (ps.size() > index) map.put(key, ps.get(index).trim());
                            }

                            list.add(map);
                        }
                        int counter = 0;
                        for (Map<String, String> stringStringMap : new ArrayList<>(list)) {
                            boolean img = true;
                            String lastValue = "";
                            for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                                lastValue = entry.getValue();
                                if (!entry.getValue().matches(".*\\.(jpg|png)")) {
                                    img = false;
                                    break;
                                }
                                if (!lastValue.isEmpty() && !lastValue.equals(entry.getValue())) {
                                    img = false;
                                    break;
                                }
                                lastValue = entry.getValue();
                            }
                            if (img) {
                                Map<String, String> map = new HashMap<>();
                                map.put("img", lastValue);
                                list.set(counter, map);
                            }
                            counter++;
                        }
                        String description = null;
                        if (new File(subFile, ".description").exists()) {
                            description = Helper.getTextFromFile(new File(subFile, ".description"),
                                    Config.getConfig(controller.getFile()).enc1());
                        }
                        item = new Chapter(subFile.getName(), description, list);
                    } else {
                        item = new Chapter(subFile.getName(), ChapterCellFactory.getChapters(subFile, controller));
                    }
                    items.add(item);
                }
            }
        }
        items.sort((o1, o2) -> {
            String first = o1.getChapterName();
            String second = o2.getChapterName();
            if (first.matches(CHAPTER_REGEX)) {
                first = first.split("# ")[0];
            }
            if (second.matches(CHAPTER_REGEX)) {
                second = second.split("# ")[0];
            }
            if (first.length() == second.length()) {
                return first.compareTo(second);
            } else {
                return first.length() - second.length();
            }
        });
        return items;
    }

}