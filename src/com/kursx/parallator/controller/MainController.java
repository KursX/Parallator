package com.kursx.parallator.controller;

import com.google.gson.Gson;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.kursx.parallator.*;
import com.kursx.parallator.factrory.ChapterCellFactory;
import com.kursx.parallator.factrory.ParagraphCellFactory;
import com.kursx.parallator.factrory.ValueFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class MainController implements Initializable {

    private List<Function<Void, Void>> undo = new ArrayList<>();

    @FXML
    private TableView<Map<String, String>> table;

    @FXML
    private TreeView<File> chapters;

    private Map<String, List<String>> textMap;
    private Map<String, TableColumn<Map<String, String>, String>> columnMap;
    private List<String> keysFilter = new ArrayList<>();
    public ChapterCellFactory chapterCellFactory;

    @FXML
    private Label statistics;

    @FXML
    private RadioButton red;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button addText;

    private VirtualFlow virtualFlow;
    public File file, chasedFile;
    private boolean edited = false;

    public void open(File file) {
        this.file = file;
        chapterCellFactory = new ChapterCellFactory(this, chapters, file);
        chapters.setCellFactory(chapterCellFactory);
    }

    public ArrayList<String> getKeys() {
        return new ArrayList<>(textMap.keySet());
    }

    public void statistics() {
        new Thread(() -> {
            if (!init || getKeys().isEmpty()) return;
            Config config = getConfig();
            int enLength = 0;
            for (File subFile : ChapterCellFactory.getFilesList(file)) {
                File file = new File(subFile, getKeys().get(0) + ".txt");
                if (file.exists()) {
                    String en = Helper.getTextFromFile(file, config.enc1()).trim();
                    enLength += en.length();
                }
            }
            final int enL = enLength;
            Platform.runLater(() -> System.out.println(getKeys().get(0) + ": " + enL));
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        table.setSelectionModel(null);
        Platform.runLater(() -> {
            TableViewSkin tableSkin = (TableViewSkin) table.getSkin();
            virtualFlow = (VirtualFlow) tableSkin.getChildren().get(1);
        });
    }

    private int progressCounter = 0;

    public int startProgress(String text) {
        Platform.runLater(() -> {
            progressLabel.setText(text + "...  ");
            progressLabel.setVisible(true);
            progressIndicator.setVisible(true);
        });
        return ++progressCounter;
    }

    public void stopProgress(int id) {
        if (progressCounter == id) {
            Platform.runLater(() -> {
                progressLabel.setVisible(false);
                progressIndicator.setVisible(false);
            });
        }
    }

    public void showChapter(File chapterFile) {
        undo.clear();
        init = false;
        chasedFile = chapterFile;
        textMap = new HashMap<>();
        columnMap = new HashMap<>();
        List<String> allKeys = new ArrayList<>();
        table.getColumns().clear();
        Config config = getConfig();

        float count = 0;
        File[] files = chapterFile.listFiles();
        if (files == null) return;
        for (File textFile : files) {
            if (textFile.getName().matches("^.*.txt$")) {
                count++;
            }
        }
        for (File textFile : files) {
            if (textFile.getName().matches("^.*.txt$")) {
                String key = StringUtils.getBaseName(textFile.getName());
                allKeys.add(key);
                if (keysFilter.contains(key)) continue;
                TableColumn<Map<String, String>, String> column = new TableColumn<>();
                column.setCellFactory(new ParagraphCellFactory(this, key));
                column.setCellValueFactory(new ValueFactory(key));
                column.prefWidthProperty().bind(table.widthProperty().multiply(1 / count));
                table.getColumns().add(column);
                String text = Helper.getTextFromFile(new File(chapterFile, key + ".txt"), config.enc1()).trim();
                List<String> list = new LinkedList<>(Arrays.asList(text.split(config.dividerRegex())));
                textMap.put(key, list);
                columnMap.put(key, column);
            }
        }

        init = true;
        show();

        boolean isRed = getConfig().isRed();
        red.setSelected(isRed);
        red.setOnAction(event -> {
            boolean checked = red.isSelected();
            getConfig().setRed(checked, getFile());
            show();
        });
        addText.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_add_paragraphs.fxml"));
                Stage dialogStage = new Stage();
                dialogStage.setScene(new Scene(loader.load()));
                AddParagraphsDialogController dialogController = loader.getController();
                dialogController.show(dialogStage, this);
            } catch (IOException e) {
                Logger.exception(e);
            }
        });
        Main.getMain().getLangs().getItems().clear();
        for (String key : allKeys) {
            CheckMenuItem item = new CheckMenuItem(key);
            item.setSelected(!keysFilter.contains(key));
            Main.getMain().getLangs().getItems().add(item);
            item.setOnAction(event -> {
                if (item.isSelected()) {
                    keysFilter.remove(key);
                } else {
                    keysFilter.add(key);
                }
                showChapter(chasedFile);
            });
        }
        table.scrollTo(getConfig().getBookmark(chasedFile.getAbsolutePath()));
    }

    public void showChapter() {
        showChapter(chasedFile);
    }

    public void nextChapter(int position, String key) {
        List<String> lines = new ArrayList<>();
        List<String> all = new ArrayList<>(textMap.get(key));
        for (int i = position; i < all.size(); i++) {
            lines.add(all.get(i));
            textMap.get(key).remove(all.get(i));
        }

        File nextFile = null;
        for (File next : chasedFile.getParentFile().listFiles()) {
            if (StringUtils.getBaseName(next.getAbsolutePath()).split("#")[0].equals(
                    (Integer.parseInt(StringUtils.getBaseName(chasedFile.getAbsolutePath()).split("#")[0]) + 1) + "")) {
                nextFile = new File(next, key + ".txt");
                break;
            }
        }

        final String text;
        if (nextFile == null) {
            text = "";
            nextFile = new File(new File(chasedFile.getParentFile(), ((Integer.parseInt(StringUtils.getBaseName(chasedFile.getAbsolutePath()).split("#")[0]) + 1) + "") + "# "), key + ".txt");
            nextFile.mkdir();
        } else {
            text = Helper.getTextFromFile(nextFile, getConfig().enc1());
        }
        try {
            String str = getConfig().divider();
            FileWriter fileWriter1 = new FileWriter(nextFile);
            for (String line : lines) {
                fileWriter1.append(line).append(str);
            }
            fileWriter1.append(text);
            fileWriter1.flush();
            fileWriter1.close();
        } catch (IOException e) {
            Logger.exception(e);
        }

        final File finalNextFile = nextFile;
        undo.add(0, number -> {
            textMap.get(key).addAll(position, all);
            try {
                FileWriter fileWriter1 = new FileWriter(finalNextFile);
                fileWriter1.append(text);
                fileWriter1.flush();
                fileWriter1.close();
            } catch (IOException e) {
                Logger.exception(e);
            }
            return null;
        });
        edit();
        show();
    }

    public void remove(int position, String key) {
        String oldLine = textMap.get(key).get(position);
        textMap.get(key).remove(position);
        undo.add(0, number -> {
            textMap.get(key).add(position, oldLine);
            return null;
        });
        edit();
        show();
    }

    public void up(int position, String key) {
        List<String> list = textMap.get(key);
        String oldLine1 = list.get(position);
        String oldLine2 = list.get(position + 1);
        String line = oldLine1 + " " + oldLine2;
        list.set(position + 1, line);
        list.remove(position);
        undo.add(0, number -> {
            textMap.get(key).add(position, oldLine1);
            textMap.get(key).set(position + 1, oldLine2);
            return null;
        });
        edit();
        show();
    }

    public void down(int position, String key) {
        List<String> list = textMap.get(key);
        String oldLine1 = list.get(position - 1);
        String oldLine2 = list.get(position);
        String line = oldLine1 + " " + oldLine2;
        list.set(position - 1, line);
        list.remove(position);
        undo.add(0, number -> {
            textMap.get(key).set(position - 1, oldLine1);
            textMap.get(key).add(position, oldLine2);
            return null;
        });
        edit();
        show();
    }

    public void update(int position, String key, String text) {
        final String oldText = textMap.get(key).get(position);
        textMap.get(key).set(position, text);
        undo.add(0, number -> {
            textMap.get(key).set(position, oldText);
            return null;
        });
        edit();
        show();
    }

    public void separate(int position, String key, String paragraph) {
        List<String> list = textMap.get(key);
        String oldLine = textMap.get(key).get(position);

        List<String> parts = PartsSeparator.getParts(paragraph, true, null);

        list.remove(position);

        for (int i = parts.size() - 1; i >= 0; i--) {
            list.add(position, parts.get(i));
        }

        undo.add(0, number -> {
            for (int i = parts.size() - 1; i >= 0; i--) {
                list.remove(position);
            }
            textMap.get(key).set(position, oldLine);
            return null;
        });
        edit();
        show();
    }

    public void separate(int position, String key, List<String> parts, int index) {
        List<String> list = textMap.get(key);
        String oldLine = textMap.get(key).get(position);
        String first = "", second = "";
        for (int i = 0; i < parts.size(); i++) {
            if (i < index) {
                first += parts.get(i) + " ";
            } else {
                second += parts.get(i) + " ";
            }
        }
        list.remove(position);
        list.add(position, second.trim());
        list.add(position, first.trim());
        undo.add(0, number -> {
            list.remove(position);
            textMap.get(key).set(position, oldLine);
            return null;
        });
        edit();
        show();
    }

    public void save() {
        final int progress = startProgress("Идет сохранение текущей главы");
        new Thread(() -> {
            undo.clear();
            edited = false;
            for (String key : getKeys()) {
                StringBuilder builder = new StringBuilder();
                for (String en : textMap.get(key)) {
                    builder.append(en).append(getConfig().divider());
                }
                try {
                    Writer writer1 = new OutputStreamWriter(
                            new FileOutputStream(new File(chasedFile, key + ".txt")), getConfig().enc1());
                    writer1.append(builder);
                    writer1.flush();
                    writer1.close();
                } catch (IOException e) {
                    Logger.exception(e);
                }
            }
            stopProgress(progress);
        }).start();
    }

    public void up() {
        int last = virtualFlow.getFirstVisibleCell().getIndex();
        if (last > 0) table.scrollTo(last - 1);
    }

    public void down() {
        int last = virtualFlow.getFirstVisibleCell().getIndex();
        table.scrollTo(last + 1);
    }

    private boolean init = false;

    public void show() {
        if (!init) return;
        ObservableList<Map<String, String>> lines = FXCollections.observableArrayList();
        int maxSize = 0;
        for (String key : getKeys()) {
            int size = textMap.get(key).size();
            maxSize = maxSize < size ? size : maxSize;
            columnMap.get(key).setText(key + " (" + size + ")");
        }

        for (int i = 0; i < maxSize; i++) {
            lines.add(new HashMap<>());
        }
        for (String key : getKeys()) {
            List<String> text = textMap.get(key);
            for (int i = 0; i < text.size(); i++) {
                lines.get(i).put(key, text.get(i));
            }
        }

        table.setItems(lines);
        statistics();
    }

    public File getFile() {
        return file;
    }

    public List<Chapter> validate() {
        try {
            return feelChapters(file);
        } catch (ValidationException e) {
            return null;
        }
    }

    public List<Chapter> feelChapters(File file) throws ValidationException {
        Config config = getConfig();
        List<Chapter> chapters = new ArrayList<>();
        if (file.isDirectory()) {
            List<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));
            new ArrayList<>(files).stream().filter(chapter -> !chapter.isDirectory()).forEach(files::remove);
            files.sort((o1, o2) -> {
                String first = StringUtils.getBaseName(o1.getAbsolutePath());
                String second = StringUtils.getBaseName(o2.getAbsolutePath());
                if (first.matches(ChapterCellFactory.CHAPTER_REGEX)) {
                    first = first.split("# ")[0];
                }
                if (second.matches(ChapterCellFactory.CHAPTER_REGEX)) {
                    second = second.split("# ")[0];
                }
                if (first.length() == second.length()) {
                    return first.compareTo(second);
                } else {
                    return first.length() - second.length();
                }
            });

            for (File subFile : files) {
                if (ChapterCellFactory.isChapter(subFile)) {
                    Map<String, List<String>> paragraphs = new HashMap<>();
                    for (String key : getKeys()) {
                        String en;
                        if (key.equals("en"))
                            en = Helper.getTextFromFile(new File(subFile, key + ".txt"), config.enc1()).trim();
                        else
                            en = Helper.getTextFromFile(new File(subFile, key + ".txt"), config.enc1()).trim();

                        String[] ens = en.split(config.dividerRegex());
                        paragraphs.put(key, new ArrayList<>(Arrays.asList(ens)));
                    }

                    for (String key : getKeys()) {
                        if (paragraphs.get(key).size() != paragraphs.get(getKeys().get(0)).size()) {
                            throw new ValidationException();
                        }
                    }
                    List<Map<String, String>> list = new ArrayList<>();
                    for (int index = 0; index < paragraphs.get(getKeys().get(0)).size(); index++) {
                        Map<String, String> map = new HashMap<>();
                        for (String key : getKeys()) {
                            List<String> ps = paragraphs.get(key);
                            if (ps.size() > index) map.put(key, ps.get(index).trim());
                        }

                        list.add(map);
                    }
                    Chapter chapter = new Chapter(subFile.getName(), null, list);
                    chapters.add(chapter);
                } else {
                    Chapter chapter = new Chapter(subFile.getName(), feelChapters(subFile));
                    chapters.add(chapter);
                }
            }
        }
        return chapters;
    }

    public void edit() {
        edited = true;
    }

    public boolean isEdited() {
        return edited;
    }

    public Config getConfig() {
        return Config.getConfig(file);
    }

    public Book getBook() {
        return new Gson().fromJson(Helper.getTextFromFile(new File(file, "book.json"), getConfig().enc1()), Book.class);
    }

    public void redraw() {
        table.refresh();
    }

    public Map<String, List<String>> getTextMap() {
        return textMap;
    }

    public void undo() {
        undo.get(0).apply(null);
        undo.remove(0);
        show();
    }
}
