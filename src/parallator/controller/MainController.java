package parallator.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import parallator.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    private List<String> ens, rus;
    private File file, chasedFile;
    private List<File> list;
    private boolean edited = false;

    public void open(File file) {
        this.file = file;
        ObservableList<Chapter> lines = FXCollections.observableArrayList();
        list = new ArrayList<>();
        for (File file1 : file.listFiles()) {
            if (file1.isDirectory()) {
                try {
                    Integer.parseInt(file1.getName());
                    list.add(file1);
                } catch (Exception ignored) {
                }
            }
        }
        Collections.sort(list, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        for (File s : list) {
            lines.add(new Chapter(s.getName()));
        }
        chapter.setCellValueFactory(new PropertyValueFactory<>("chapterName"));
        chapters.setItems(lines);
        chapter.setCellFactory(param -> {
            TableCell<Chapter, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(cell.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            text.setOnMouseClicked(t -> {
                if (t.getButton() == MouseButton.PRIMARY) {
                    change(list.get(cell.getIndex()));
                    getConfig().setLastChapter(cell.getIndex());
                }
            });
            return cell;
        });
        change(list.get(getConfig().getLastChapter()));
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        input.setCellValueFactory(new PropertyValueFactory<>("en"));
        output.setCellValueFactory(new PropertyValueFactory<>("ru"));

        input.setCellFactory(new CellFactory(this, true));
        output.setCellFactory(new CellFactory(this, false));

        input.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        output.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        chapter.prefWidthProperty().bind(chapters.widthProperty().multiply(1));
    }

    public void change(File chapterFile) {
        chasedFile = chapterFile;
        Config config = getConfig();

        String en = Helper.getTextFromFile(new File(chapterFile, "1.txt"), config.enc1()).trim();
        String ru = Helper.getTextFromFile(new File(chapterFile, "2.txt"), config.enc2()).trim();

        ens = new LinkedList<>(Arrays.asList(en.split(config.divider())));
        rus = new LinkedList<>(Arrays.asList(ru.split(config.divider())));

        input.setText("Исходник (" + ens.size() + ")");
        output.setText("Перевод (" + rus.size() + ")");

        show();
    }

    public void change() {
        change(chasedFile);
    }

    public void remove(int position, boolean left) {
        if (left) {
            ens.remove(position);
        } else {
            rus.remove(position);
        }
        show();
    }

    public void up(int position, boolean left) {
        List<String> list = left ? ens : rus;
        String line = list.get(position) + " " + list.get(position + 1);
        list.set(position + 1, line);
        list.remove(position);
        show();
    }

    public void down(int position, boolean left) {
        List<String> list = left ? ens : rus;
        String line = list.get(position - 1) + " " + list.get(position);
        list.set(position - 1, line);
        list.remove(position);
        show();
    }

    public void separate(int position, boolean left, String[] parts, int index) {
        List<String> list = left ? ens : rus;
        String first = "", second = "";
        for (int i = 0; i < parts.length; i++) {
            if (i < index) {
                first += parts[i] + ".";
            } else {
                second += parts[i] + ".";
            }
        }
        list.remove(position);
        list.add(position, second.trim());
        list.add(position, first.trim());
        show();
    }

    public void save() {
        if (ens == null) return;
        StringBuilder enBuilder = new StringBuilder();
        StringBuilder ruBuilder = new StringBuilder();
        for (String en : ens) {
            enBuilder.append(en).append("\n\n");
        }

        for (String ru : rus) {
            ruBuilder.append(ru).append("\n\n");
        }

        try {
            List<Paragraph> lines = new ArrayList<>();
            for (int i = 0; i < (ens.size() > rus.size() ? ens.size() : rus.size()); i++) {
                lines.add(new Paragraph(i < ens.size() ? ens.get(i) : "", i < rus.size() ? rus.get(i) : ""));
            }
            FileWriter wrt = new FileWriter(new File(chasedFile, "1.txt"));
            wrt.append(enBuilder);
            wrt.flush();

            wrt = new FileWriter(new File(chasedFile, "2.txt"));
            wrt.append(ruBuilder);
            wrt.flush();

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
            Chapter chapter = new Chapter("Chapter ", null, paragraphs);
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
    
    public Config getConfig()  {
        return Helper.getConfig(file);
    }
}
