package parallator.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import parallator.Chapter;
import parallator.Config;
import parallator.Helper;
import parallator.Paragraph;
import parallator.factrory.ChapterCellFactory;
import parallator.factrory.ParagraphCellFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    @FXML
    private TableView<Paragraph> table;

    @FXML
    private TableView<Chapter> chapters;

    @FXML
    private TableColumn<Chapter, String> chapter;

    @FXML
    private TableColumn<Paragraph, String> input, output;

    @FXML
    private Label digitsEn, paragraphs, words;

    private List<String> ens, rus;
    private File file, chasedFile;
    private boolean edited = false;
    private List<File> list;

    public void open(File file) {
        this.file = file;
        chapter.setCellValueFactory(new PropertyValueFactory<>("chapterName"));
        list = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) for (File file1 : files) {
            if (file1.isDirectory()) {
                try {
                    Integer.parseInt(file1.getName());
                    list.add(file1);
                } catch (Exception ignored) {
                }
            }
        }
        Collections.sort(list, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        chapter.setCellFactory(new ChapterCellFactory(this, chapters));
        chapters.getSelectionModel().select(getConfig().getLastChapter());
    }

    public void statistics() {
        new Thread(() -> {
            Config config = getConfig();
            int enLength = 0, paragraphs = 0, words = 0;
            for (File file1 : list) {
                if (!file1.getAbsolutePath().equals(file.getAbsolutePath())) {
                    String en = Helper.getTextFromFile(new File(file1, "1.txt"), config.enc1()).trim();
                    enLength += en.length();
                    paragraphs += en.split(config.divider()).length;
                    words += en.split("[ .,:;\"?()!-]").length;
                }
            }
            for (String en : ens) {
                enLength += en.length();
                words += en.split("[ .,:;\"?()!-]").length;
            }
            paragraphs += ens.size();
            final int enL = enLength, p = paragraphs, w = words;
            Platform.runLater(() -> {
                digitsEn.setText(enL + "");
                MainController.this.paragraphs.setText(p + "");
                MainController.this.words.setText(w + "");
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
        chapter.prefWidthProperty().bind(chapters.widthProperty().multiply(1));

        table.setSelectionModel(null);
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

        chapters.getSelectionModel().select(getConfig().getLastChapter());

        show();
    }

    public void showChapter(int index) {
        getConfig().setLastChapter(index);
        showChapter(getFilesList().get(index));
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

//            List<Paragraph> lines = new ArrayList<>();
//            for (int i = 0; i < (ens.size() > rus.size() ? ens.size() : rus.size()); i++) {
//                lines.add(new Paragraph(i < ens.size() ? ens.get(i) : "", i < rus.size() ? rus.get(i) : ""));
//            }
//            wrt = new FileWriter(new File(chasedFile, chasedFile.getName() + ".json"));
//            wrt.append(new Gson().toJson(lines));
//            wrt.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        List<Chapter> chapters = new ArrayList<>();
        Config config = getConfig();
        int chapterNumber = 1;
        for (File file1 : list) {
            List<Paragraph> paragraphs = new ArrayList<>();

            String en = Helper.getTextFromFile(new File(file1, "1.txt"), config.enc1()).trim();
            String ru = Helper.getTextFromFile(new File(file1, "2.txt"), config.enc2()).trim();
            String[] ens = en.split(config.divider());
            String[] rus = ru.split(config.divider());
            if (ens.length != rus.length) {
                return null;
            }

            for (int index = 0; index < ens.length; index++) {
                paragraphs.add(new Paragraph(ens[index], rus[index]));
            }
            Chapter chapter = new Chapter("Chapter " + chapterNumber++, null, paragraphs);
            chapters.add(chapter);
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

    public List<File> getFilesList() {
        return list;
    }
}
