package parallator.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parallator.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class ChapterDialogController implements Initializable {

    private Stage stage;
    private Main main;

    public void init(Stage stage, Main main) {
        this.stage = stage;
        this.main = main;
    }

    @FXML
    TextField chapter, description;

    @FXML
    Button cancel, save;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancel.setOnAction(event -> {

        });
        save.setOnAction(event -> {
            save();
            stage.close();
        });
        cancel.setOnAction(event -> stage.close());

    }

    public boolean save() {

        return true;
    }

}
