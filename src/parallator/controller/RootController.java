package parallator.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import parallator.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RootController implements Initializable {

    private List<String> ens, rus;
    private File file, chasedFile;
    private Stage stage;
    private List<File> list;

    public void open() {
        file = Helper.showDirectoryChooser(stage.getScene());
        if (file != null) {
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
                    } else if (t.getButton() == MouseButton.SECONDARY) {
                        change(list.get(cell.getIndex()));
                    }
                });
                return cell;
            });
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private TableView<Paragraph> table;

    @FXML
    private TableView<Chapter> chapters;

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
        chapter.prefWidthProperty().bind(chapters.widthProperty().multiply(1));
    }

    public void change(File file) {
        chasedFile = file;
        Config config = Helper.getConfig(file.getParentFile());
        String en = Helper.getTextFromFile(new File(file, "1.txt"), config.enc1).trim();
        String ru = Helper.getTextFromFile(new File(file, "2.txt"), config.enc2).trim();

        ens = new LinkedList<>(Arrays.asList(en.split("(\\n\\n|\\n *\\n)")));
        rus = new LinkedList<>(Arrays.asList(ru.split("\\n\\n")));

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
    }

    public File getFile() {
        return file;
    }

    public boolean validate() {
        for (File file1 : list) {
            String en = Helper.getTextFromFile(new File(file1, "1.txt"), Helper.getConfig(file1.getParentFile()).enc1).trim();
            String ru = Helper.getTextFromFile(new File(file1, "2.txt"), Helper.getConfig(file1.getParentFile()).enc2).trim();
            if (en.split("\\n\\n").length != ru.split("\\n\\n").length) {
                return false;
            }
        }
        return true;
    }
}
