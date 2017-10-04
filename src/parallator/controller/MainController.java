package parallator.controller;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import parallator.*;
import parallator.factrory.ChapterCellFactory;
import parallator.factrory.ParagraphCellFactory;
import parallator.factrory.ValueFactory;

import javax.xml.bind.ValidationException;
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
    private List<String> keys;
    private List<String> keysFilter = new ArrayList<>();

    @FXML
    private Label statistics;

    @FXML
    private RadioButton red;

    private VirtualFlow virtualFlow;
    private File file, chasedFile;
    private boolean edited = false;

    public void open(File file) {
        this.file = file;
        chapters.setCellFactory(new ChapterCellFactory(this, chapters, file));
    }

    public void statistics() {
        new Thread(() -> {
            if (!init) return;
            Config config = getConfig();
            int enLength = 0, paragraphs = 0, words = 0;
            for (File subFile : ChapterCellFactory.getFilesList(file)) {
                File file = new File(subFile, keys.get(0) + ".txt");
                if (file.exists()) {
                    String en = Helper.getTextFromFile(file, config.enc1()).trim();
                    enLength += en.length();
                    paragraphs += en.split(config.dividerRegex()).length;
                    words += en.split("[ .,:;\"?()!-]").length;
                }
            }
            final int enL = enLength, p = paragraphs, w = words;
            float quality[] = getPercent(file);
            Platform.runLater(() -> {
                MainController.this.statistics.setText(
                        String.format("Исходник Количество символов: %1$d Параграфоф: %2$d Слов: %3$d Оценка разбития: %4$.2f%%",
                                enL, p, w, (quality[1] - quality[0]) * 100 / quality[1]));
            });
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

    public void showChapter(File chapterFile) {
        undo.clear();
        init = false;
        chasedFile = chapterFile;
        textMap = new HashMap<>();
        keys = new ArrayList<>();
        columnMap = new HashMap<>();
        List<String> allKeys = new ArrayList<>();
        table.getColumns().clear();
        Config config = getConfig();

        float count = 0;
        for (File textFile : chapterFile.listFiles()) {
            if (textFile.getName().matches("^.*.txt$")) {
                count++;
            }
        }
        for (File textFile : chapterFile.listFiles()) {
            if (textFile.getName().matches("^.*.txt$")) {
                String key = StringUtils.getBaseName(textFile.getName());
                allKeys.add(key);
                if (keysFilter.contains(key)) continue;
                TableColumn<Map<String, String>, String> column = new TableColumn<>();
                column.setCellFactory(new ParagraphCellFactory(this, key));
                column.setCellValueFactory(new ValueFactory(key));
                column.prefWidthProperty().bind(table.widthProperty().multiply(1 / count));
                table.getColumns().add(column);

                keys.add(key);

                String text = Helper.getTextFromFile(new File(chapterFile, key + ".txt"), config.enc1()).trim();
                List<String> list = new LinkedList<>(Arrays.asList(text.split(config.dividerRegex())));
                textMap.put(key, list);
                columnMap.put(key, column);
            }
        }

        init = true;
        show();

        red.setSelected(getConfig().isRed());
        red.setOnAction(event -> {
            getConfig().setRed(red.isSelected());
            show();
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
            e.printStackTrace();
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
                e.printStackTrace();
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

    public void separate(int position, String key, List<String> parts, int index, String divider) {
        List<String> list = textMap.get(key);
        String oldLine = textMap.get(key).get(position);
        String first = "", second = "";
        for (int i = 0; i < parts.size(); i++) {
            if (i < index) {
                first += parts.get(i) + divider;
            } else {
                second += parts.get(i) + divider;
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
        undo.clear();
        edited = false;
        for (String key : keys) {
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
                e.printStackTrace();
            }
        }
    }

    public void up() {
        int last = virtualFlow.getFirstVisibleCellWithinViewPort().getIndex();
        if (last > 0) table.scrollTo(last - 1);
    }

    public void down() {
        int last = virtualFlow.getFirstVisibleCellWithinViewPort().getIndex();
        table.scrollTo(last + 1);
    }

    private boolean init = false;

    public void show() {
        if (!init) return;
        ObservableList<Map<String, String>> lines = FXCollections.observableArrayList();
        int maxSize = 0;
        for (String key : keys) {
            int size = textMap.get(key).size();
            maxSize = maxSize < size ? size : maxSize;
            columnMap.get(key).setText(key + " (" + size + ")");
        }

        for (int i = 0; i < maxSize; i++) {
            lines.add(new HashMap<>());
        }
        for (String key : keys) {
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
            new ArrayList<>(files).stream().filter(chapter -> !ChapterCellFactory.isChapter(chapter)).forEach(files::remove);
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
                    for (String key : keys) {
                        String en = Helper.getTextFromFile(new File(subFile, key + ".txt"), config.enc1()).trim();
                        String[] ens = en.split(config.dividerRegex());
                        paragraphs.put(key, new ArrayList<>(Arrays.asList(ens)));
                    }

                    for (String key : keys) {
                        if (paragraphs.get(key).size() != paragraphs.get(keys.get(0)).size()) throw new ValidationException("");
                    }
                    List<Map<String, String>> list = new ArrayList<>();
                    for (int index = 0; index < paragraphs.get(keys.get(0)).size(); index++) {
                        Map<String, String> map = new HashMap<>();
                        for (String key : keys) {
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

    public float[] getPercent(File file) {
        float[] arr = new float[2];
        Config config = getConfig();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (ChapterCellFactory.isChapter(subFile)) {
                        for (String key : keys) {
                            File textFile = new File(subFile, key + ".txt");
                            if (textFile.exists()) {
                                String en = Helper.getTextFromFile(textFile, config.enc1()).trim();

                                for (String s : en.split(config.dividerRegex())) {
                                    List<String> parts = ParagraphCellFactory.getParts(s);
                                    if (parts.size() > 5) arr[0]++;
                                    arr[1]++;
                                }
                            }
                        }
                    } else {
                        float[] arr1 = getPercent(subFile);
                        arr[0] += arr1[0];
                        arr[1] += arr1[1];
                    }
                }
            }
        }
        return arr;
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
