package parallator.controller;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import parallator.Chapter;
import parallator.Config;
import parallator.Helper;
import parallator.Paragraph;
import parallator.factrory.ChapterCellFactory;
import parallator.factrory.ParagraphCellFactory;

import javax.xml.bind.ValidationException;
import java.io.*;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    @FXML
    private TableView<Paragraph> table;

    @FXML
    private TreeView<File> chapters;

    @FXML
    private TableColumn<Paragraph, String> input, output;

    @FXML
    private Label statistics;

    @FXML
    RadioButton red;

    private VirtualFlow virtualFlow;
    private List<String> ens, rus;
    private File file, chasedFile;
    private boolean edited = false;

    public void open(File file) {
        this.file = file;
        chapters.setCellFactory(new ChapterCellFactory(this, chapters, file));
    }

    public void statistics() {
        new Thread(() -> {
            Config config = getConfig();
            int enLength = 0, paragraphs = 0, words = 0;
            for (File subFile : ChapterCellFactory.getFilesList(file)) {
                String en = Helper.getTextFromFile(new File(subFile, "1.txt"), config.enc1()).trim();
                enLength += en.length();
                paragraphs += en.split(config.divider()).length;
                words += en.split("[ .,:;\"?()!-]").length;
            }
            final int enL = enLength, p = paragraphs, w = words;
            float quality[] = getPercent(file);
            Platform.runLater(() -> {
                MainController.this.statistics.setText(
                        String.format("Исходник Количество символов: %1$d Параграфоф: %2$d Слов: %3$d Оценка разбития: %4$.2f%%",
                                enL, p, w, (quality[1] - quality[0]) * 100 / quality[1] ));
            });
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        input.setCellValueFactory(new PropertyValueFactory<>("en"));
        output.setCellValueFactory(new PropertyValueFactory<>("ru"));

        input.setCellFactory(new ParagraphCellFactory(this, true));
        output.setCellFactory(new ParagraphCellFactory(this, false));

        input.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        output.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        table.setSelectionModel(null);

        Platform.runLater(() -> {
            TableViewSkin tableSkin = (TableViewSkin) table.getSkin();
            virtualFlow = (VirtualFlow) tableSkin.getChildren().get(1);
        });

    }

    public void showChapter(File chapterFile) {
        chasedFile = chapterFile;
        Config config = getConfig();

        String en = Helper.getTextFromFile(new File(chapterFile, "1.txt"), config.enc1()).trim();
        String ru = Helper.getTextFromFile(new File(chapterFile, "2.txt"), config.enc2()).trim();

        ens = new LinkedList<>(Arrays.asList(en.split(config.divider())));
        rus = new LinkedList<>(Arrays.asList(ru.split(config.divider())));

        input.setText("Исходник (" + ens.size() + ")");
        output.setText("Перевод (" + rus.size() + ")");

        show();

        red.setSelected(getConfig().isRed());
        red.setOnAction(event -> {
            getConfig().setRed(red.isSelected());
            show();
        });
    }

    public void showChapter() {
        showChapter(chasedFile);
    }

    public void remove(int position, boolean left) {
        if (left) {
            ens.remove(position);
        } else {
            rus.remove(position);
        }
        edit();
        show();
    }

    public void up(int position, boolean left) {
        List<String> list = left ? ens : rus;
        String line = list.get(position) + " " + list.get(position + 1);
        list.set(position + 1, line);
        list.remove(position);
        edit();
        show();
    }

    public void down(int position, boolean left) {
        List<String> list = left ? ens : rus;
        String line = list.get(position - 1) + " " + list.get(position);
        list.set(position - 1, line);
        list.remove(position);
        edit();
        show();
    }

    public void update(int position, boolean left, String text) {
        List<String> list = left ? ens : rus;
        list.set(position, text);
        edit();
        show();
    }

    public void separate(int position, boolean left, List<String> parts, int index, String divider) {
        List<String> list = left ? ens : rus;
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
        edit();
        show();
    }

    public void save() {
        if (ens == null) return;
        edited = false;
        StringBuilder enBuilder = new StringBuilder();
        StringBuilder ruBuilder = new StringBuilder();
        for (String en : ens) {
            enBuilder.append(en).append("\n\n");
        }

        for (String ru : rus) {
            ruBuilder.append(ru).append("\n\n");
        }

        try {
            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream(new File(chasedFile, "1.txt")), getConfig().enc1());
            writer1.append(enBuilder);
            writer1.flush();

            Writer writer2 = new OutputStreamWriter(
                    new FileOutputStream(new File(chasedFile, "2.txt")), getConfig().enc2());
            writer2.append(ruBuilder);
            writer2.flush();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void show() {
        ObservableList<Paragraph> lines = FXCollections.observableArrayList();
        for (int i = 0; i < (ens.size() > rus.size() ? ens.size() : rus.size()); i++) {
            lines.add(new Paragraph(i < ens.size() ? ens.get(i) : "", i < rus.size() ? rus.get(i) : ""));
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
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (ChapterCellFactory.isChapter(subFile)) {
                        List<Paragraph> paragraphs = new ArrayList<>();

                        String en = Helper.getTextFromFile(new File(subFile, "1.txt"), config.enc1()).trim();
                        String ru = Helper.getTextFromFile(new File(subFile, "2.txt"), config.enc2()).trim();
                        String[] ens = en.split(config.divider());
                        String[] rus = ru.split(config.divider());

                        if (ens.length != rus.length) {
                            throw new ValidationException("");
                        }

                        for (int index = 0; index < ens.length; index++) {
                            paragraphs.add(new Paragraph(ens[index], rus[index]));
                        }
                        Chapter chapter = new Chapter(subFile.getName(), null, paragraphs);
                        chapters.add(chapter);
                    } else {
                        Chapter chapter = new Chapter(subFile.getName(), feelChapters(subFile));
                        chapters.add(chapter);
                    }
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

                        String en = Helper.getTextFromFile(new File(subFile, "1.txt"), config.enc1()).trim();
                        String ru = Helper.getTextFromFile(new File(subFile, "2.txt"), config.enc2()).trim();

                        for (String s : en.split(config.divider())) {
                            List<String> parts = ParagraphCellFactory.getParts(s);
                            if (parts.size() > 5) arr[0]++;
                            arr[1]++;
                        }
                        for (String s : ru.split(config.divider())) {
                            List<String> parts = ParagraphCellFactory.getParts(s);
                            if (parts.size() > 5) arr[0]++;
                            arr[1]++;
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
}
