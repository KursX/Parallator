package parallator.factrory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;
import parallator.Config;
import parallator.controller.EditDialogController;
import parallator.controller.MainController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChapterCellFactory implements Callback<TreeView<File>, TreeCell<File>> {

    private MainController controller;
    private TreeView<File> chapters;
    private File file;
    private TreeItem<File> selected;

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
        String lastChapter = controller.getConfig().getLastChapter();
        if (lastChapter != null) controller.showChapter(new File(lastChapter));
        if (selected != null) {
            int index = chapters.getRow(selected);
            chapters.getSelectionModel().select(index);
        }
    }

    private ArrayList<TreeItem<File>> getItems(File file) {
        Config config = controller.getConfig();
        ArrayList<TreeItem<File>> items = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                TreeItem<File> item;
                if (subFile.isDirectory()) {
                    item = new TreeItem<>(subFile);
                    item.getChildren().addAll(getItems(subFile));
                    items.add(item);
                    if (config.getLastChapter() != null) {
                        if (subFile.getAbsolutePath().equals(config.getLastChapter())) {
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
                                        chapters.getSelectionModel().select(index);
                                        return;
                                    }
                                }
                                controller.showChapter(item);
                                controller.getConfig().setLastChapter(item.getAbsolutePath());
                            } else {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_edit.fxml"));
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
                                    e.printStackTrace();
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
                int count = 0;
                for (File subFile : list) {
                    if (subFile.getName().equals("1.txt")) {
                        count++;
                    } else if (subFile.getName().equals("2.txt")) {
                        count++;
                    }
                }
                return count == 2;
            }
        }
        return false;
    }


}