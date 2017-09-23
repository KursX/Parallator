package parallator.controller;

import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parallator.Helper;
import parallator.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Fb2DialogController implements Initializable {

    private final Image icDelete = new Image(getClass().getResource("/resources/ic/ic_delete.png").toString());

    private MainController controller;
    private Stage stage;
    private File file1, file2, file3;
    private List<Section> sections1, sections2;
    private int leftCount, rightCount;

    public void init(Stage scene, MainController controller) {
        this.stage = scene;
        this.controller = controller;
    }

    @FXML
    VBox fromBox, toBox;

    @FXML
    Button from, to, path, load;

    private void feel(List<Section> rootSections, List<Section> sections, String tabs, VBox box, boolean left) {
        int count = 0;
        String tab = tabs + "\t";
        for (int index = 0; index < sections.size(); index++) {
            Section section = sections.get(index);
            Label label = new Label(tabs + section.toString() + " ");
            HBox hbox = new HBox();
            boolean unite = true;
            if (!section.getSections().isEmpty()) {
                for (Section sub : section.getSections()) {
                    if (!sub.getSections().isEmpty()) {
                        unite = false;
                        break;
                    }
                }
            } else {
                unite = false;
            }
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.getChildren().add(label);
            if (unite) {
                unite(section, box, rootSections, hbox, left);
            } else {
                ImageView imageView = new ImageView(icDelete);
                imageView.setOnMouseClicked(event -> {
                    sections.remove(section);
                    refresh(box, rootSections, left);
                });
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                hbox.getChildren().add(imageView);
            }
            ContextMenu menu = new ContextMenu();
            final int pos = index;
            if (index > 0) {
                MenuItem up = new MenuItem("В начало следующей главы");
                up.setOnAction(event1 -> {
                    int i = pos - 1;
                    Section subSection = sections.get(i);
                    List<P> ps = subSection.getParagraphs();
                    while (ps.isEmpty()) {
                        if (!subSection.getSections().isEmpty()) {
                            subSection = subSection.getSections().get(subSection.getSections().size() - 1);
                            ps = subSection.getParagraphs();
                        } else {
                            ps = subSection.getParagraphs();
                            break;
                        }
                    }
                    if (i > 0) {
                        List<P> paragraphs = sections.get(i).getParagraphs();
                        paragraphs.addAll(section.getParagraphs());
                        sections.remove(section);
                    }
                    refresh(box, rootSections, left);
                });
                menu.getItems().add(up);
            }

            if (index < sections.size() - 1) {
                MenuItem down = new MenuItem("В конец предыдущей главы");
                down.setOnAction(event1 -> {


                });
                menu.getItems().add(down);
            }
            label.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!section.getParagraphs().isEmpty()) {
                        String text =  getText(section.getParagraphs());
                        if (!text.isEmpty()) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layouts/dialog_text.fxml"));
                                Stage dialogStage = new Stage();
                                dialogStage.setScene(new Scene(loader.load()));
                                TextDialogController dialogController = loader.getController();
                                dialogController.show(dialogStage, text);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(stage, "Пустой параграф", 500);
                        }
                    }
                } else if (event.getButton() == MouseButton.SECONDARY){
                    if (menu.isShowing()) menu.hide();
                    menu.show(stage);
                }
            });
            count++;
            box.getChildren().add(hbox);
            if (!section.getSections().isEmpty()) {
                feel(rootSections, section.getSections(), tab, box, left);
            }
        }
        if (left) {
            leftCount = count;
        } else {
            rightCount = count;
        }
    }

    public String getText(List<P> ps) {
        String value = "";
        for (P p : ps) {
            value += p.getP() + "\n";
        }
        return value.substring(0, value.length() - "\n".length());
    }

    private void refresh(VBox box, List<Section> sections, boolean left) {
        box.getChildren().clear();
        feel(sections, sections, "", box, left);
    }

    private List<Section> readFb2(File file, VBox box, boolean left) {
        try {
            FictionBook fictionBook = new FictionBook(file);
            List<Section> sections = fictionBook.getBody().getSections();
            feel(sections, sections, "", box, left);
            return sections;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        from.setOnAction(event -> {
            file1 = Helper.showFileChooser(stage.getScene());
            fromBox.getChildren().clear();
            sections1 = readFb2(file1, fromBox, true);
        });
        to.setOnAction(event -> {
            file2 = Helper.showFileChooser(stage.getScene());
            toBox.getChildren().clear();
            sections2 = readFb2(file2, toBox, false);
        });
        path.setOnAction(event -> {
            file3 = Helper.showDirectoryChooser(stage.getScene());
        });
        load.setOnAction(event -> {
            if (file1 == null) {
                Toast.makeText(stage, "Не выбран исходник");
                return;
            }
            if (file2 == null) {
                Toast.makeText(stage, "Не выбран перевод");
                return;
            }
            if (file3 == null) {
                Toast.makeText(stage, "Не выбран путь для импорта");
                return;
            }
            if (leftCount != rightCount) {
                Toast.makeText(stage, "Количество глав не совпадает");
                return;
            }
            Helper.importFb2(sections1, sections2, file3);
            controller.open(file3);
            stage.hide();
        });
    }

    private void unite(Section section, VBox box, List<Section> sections, HBox hbox, boolean left) {
        Button unite = new Button("Объединить");
        unite.setOnMouseClicked(event -> {
            List<P> paragraphs = new ArrayList<>();
            for (Section sub : section.getSections()) {
                paragraphs.addAll(sub.getParagraphs());
            }
            section.setParagraphs(paragraphs);
            section.setSections(new ArrayList<>());
            refresh(box, sections, left);
        });
        hbox.getChildren().add(unite);
    }
}
