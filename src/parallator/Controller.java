package parallator;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    private List<String> ens, rus;
    private File file, chasedFile;
    private Scene scene;
    private List<File> list;

    public class Chapter {
        public String chapter;

        public Chapter(String chapter) {
            this.chapter = chapter;
        }

        public String getChapter() {
            return chapter;
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;


        file = Helper.showDirectoryChooser(scene);
        if (file != null) {
            ObservableList<Chapter> lines = FXCollections.observableArrayList();
            list = Arrays.asList(file.listFiles());
            Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            for (File s : list) {
                lines.add(new Chapter(s.getName()));
            }
            chapter.setCellValueFactory(new PropertyValueFactory<>("chapter"));
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
                    } else if (t.getButton() == MouseButton.SECONDARY){
                        change(list.get(cell.getIndex()));

                    }
                });
                return cell;
            });
        }
    }

    @FXML
    TableView<Paragraph> table;

    @FXML
    TableView<Chapter> chapters;

    @FXML
    private TableColumn<Chapter, String> chapter;

    @FXML
    private TableColumn<Paragraph, String> input, output;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        input.setCellValueFactory(new PropertyValueFactory<>("en"));
        output.setCellValueFactory(new PropertyValueFactory<>("ru"));
        input.setCellFactory(new CellFactory(this, true));
        output.setCellFactory(new CellFactory(this, false));

        input.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        output.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
    }

    public void change(File file) {
        save();
        chasedFile = file;
        String en = Helper.getTextFromFile(new File(file, "en.txt").getAbsolutePath());
        String ru = Helper.getTextFromFile(new File(file, "ru.txt").getAbsolutePath());

        ens = new LinkedList<>(Arrays.asList(en.split("\\n\\n")));
        rus = new LinkedList<>(Arrays.asList(ru.split("\\n\\n")));

        show();
    }

    public void remove(int position, boolean left) {
        if (left) {
            ens.remove(position);
        } else {
            rus.remove(position);
        }
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
            FileWriter wrt = new FileWriter(new File(chasedFile, "en.txt"));
            wrt.append(enBuilder);
            wrt.flush();

            wrt = new FileWriter(new File(chasedFile, "rus.txt"));
            wrt.append(ruBuilder);
            wrt.flush();

            wrt = new FileWriter(new File(chasedFile, file.getName()));
            wrt.append(new Gson().toJson(lines));
            wrt.flush();
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
    }
}
