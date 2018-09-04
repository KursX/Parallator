package com.kursx.parallator.controller;

import com.google.gson.Gson;
import com.kursx.parallator.*;
import com.kursx.parser.fb2.Binary;
import com.kursx.parser.fb2.Element;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.Section;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class Fb2DialogController implements Initializable {

    private final Image icDelete = new Image(getClass().getResource("/com/kursx/parallator/ic/ic_delete.png").toString());

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
    private Label pathLabel;

    @FXML
    ComboBox<String> fromL, toL;

    @FXML
    VBox fromBox, toBox;

    @FXML
    Button from, to, path, load;

    private Section getNextSubSection(Section section) {
        if (!section.getElements().isEmpty()) {
            return section;
        } else if (!section.getSections().isEmpty()){
            return getNextSubSection(section.getSections().get(0));
        }
        return null;
    }

    private Section getPrevSubSection(Section section) {
        if (!section.getElements().isEmpty()) {
            return section;
        }
        return null;
    }

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
            ContextMenu menu = new ContextMenu();
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


                final int pos = index;
                if (pos < sections.size() - 1) {
                    final Section nextSection = getNextSubSection(sections.get(pos + 1));
                    if (nextSection != null) {
                        MenuItem up = new MenuItem("В начало следующей главы");
                        up.setOnAction(event1 -> {
                            nextSection.getElements().addAll(0, sections.get(pos).getElements());
                            sections.remove(section);
                            refresh(box, rootSections, left);
                        });
                        menu.getItems().add(up);
                    }
                }

                if (pos > 0) {
                    final Section prevSection = getPrevSubSection(sections.get(pos - 1));
                    if (prevSection != null) {
                        MenuItem down = new MenuItem("В конец предыдущей главы");
                        down.setOnAction(event1 -> {
                            prevSection.getElements().addAll(sections.get(pos).getElements());
                            sections.remove(section);
                            refresh(box, rootSections, left);
                        });
                        menu.getItems().add(down);
                    }
                }
            }
            label.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!section.getElements().isEmpty()) {
                        String text = Element.getText(section.getElements(), "\n");
                        if (!text.isEmpty()) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kursx/parallator/layouts/dialog_edit_paragraph.fxml"));
                                Stage dialogStage = new Stage();
                                dialogStage.setScene(new Scene(loader.load()));
                                TextDialogController dialogController = loader.getController();
                                dialogController.show(dialogStage, text);
                            } catch (IOException e) {
                                Logger.exception(e);
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

    private void refresh(VBox box, List<Section> sections, boolean left) {
        box.getChildren().clear();
        feel(sections, sections, "", box, left);
    }

    private void exportImages(File book, File path) {
        try {
            FictionBook fictionBook = new FictionBook(book);
            for (Binary binary : fictionBook.getBinaries().values()) {
                byte[] b = Base64.getDecoder().decode(binary.getBinary().getBytes(StandardCharsets.US_ASCII));
                ByteArrayInputStream is = new ByteArrayInputStream(b);
                FileOutputStream outStream = new FileOutputStream(new File(path, binary.getId()));
                int length;
                byte[] buffer = new byte[1024];
                while ((length = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                }
                is.close();
                outStream.close();
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private List<Section> readFb2(File file, VBox box, boolean left) {
        try {
            FictionBook fictionBook = new FictionBook(file);
            List<Section> sections = fictionBook.getBody().getSections();
            feel(sections, sections, "", box, left);
            try {
                for (Binary binary : fictionBook.getBinaries().values()) {
                        byte[] b = Base64.getDecoder().decode(binary.getBinary().getBytes(StandardCharsets.US_ASCII));
                        ByteArrayInputStream is = new ByteArrayInputStream(b);
                        FileOutputStream outStream = new FileOutputStream(new File(file.getParent(), binary.getId()));
                        int length;
                        byte[] buffer = new byte[1024];
                        while ((length = is.read(buffer)) != -1) {
                            outStream.write(buffer, 0, length);
                        }
                        is.close();
                        outStream.close();
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
            return sections;
        } catch (Exception e) {
            Logger.exception(e);
        }
        return new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fromL.setItems(BookDialogController.langs);
        toL.setItems(BookDialogController.langs);
        fromL.getSelectionModel().select("en");
        toL.getSelectionModel().select("ru");
        from.setOnMouseClicked(event -> {
            file1 = Helper.showFileChooser(stage.getScene(), new FileChooser.ExtensionFilter("fb2", "*.fb2"));
            if (file1 == null) return;
            if (event.getButton() == MouseButton.PRIMARY) {
                fromBox.getChildren().clear();
                sections1 = readFb2(file1, fromBox, true);
            }
        });
        to.setOnMouseClicked(event -> {
            file2 = Helper.showFileChooser(stage.getScene(), new FileChooser.ExtensionFilter("fb2", "*.fb2"));
            if (file2 == null) return;
            if (event.getButton() == MouseButton.PRIMARY) {
                toBox.getChildren().clear();
                sections2 = readFb2(file2, toBox, false);
            } else if (Helper.isDebug()) {
                Book book = new Gson().fromJson(Helper.getTextFromFile(file2, Helper.UTF_8), Book.class);
                try {
                    Helper.write(book.getChapters(), file3);
                } catch (IOException e) {
                    Logger.exception(e);
                }
            }
        });
        path.setOnAction(event -> {
            file3 = Helper.showDirectoryChooser(stage.getScene());
            pathLabel.setText(file3.getAbsolutePath());
        });
        load.setOnAction(event -> {
            if (file1 == null || file2 == null) {
                Toast.makeText(stage, "Не выбран файл");
                return;
            }
            if (file3 == null) {
                Toast.makeText(stage, "Не выбран путь для импорта");
                return;
            }
            if (leftCount == 0 || rightCount == 0) {
                Toast.makeText(stage, "Нет глав");
                return;
            }
            Helper.importFb2(sections1, file3, fromL.getValue());
            Helper.importFb2(sections2, file3, toL.getValue());

            exportImages(file1, file3);
            exportImages(file2, file3);

            controller.open(file3);
            stage.hide();
            MainConfig.getMainConfig().setBookPath(file3.getAbsolutePath());
        });
    }

    private void unite(Section section, VBox box, List<Section> sections, HBox hbox, boolean left) {
        Button unite = new Button("Объединить");
        unite.setOnMouseClicked(event -> {
            ArrayList<Element> paragraphs = new ArrayList<>();
            for (Section sub : section.getSections()) {
                paragraphs.addAll(sub.getElements());
            }
            section.setElements(paragraphs);
            section.setSections(new ArrayList<>());
            refresh(box, sections, left);
        });
        hbox.getChildren().add(unite);
    }
}
