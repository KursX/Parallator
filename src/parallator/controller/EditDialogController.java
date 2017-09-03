package parallator.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EditDialogController implements Initializable {

    private Stage stage;
    private int position;
    private boolean en;
    private MainController controller;

    public void init(Stage stage, MainController controller, boolean en, int position) {
        this.stage = stage;
        this.controller = controller;
        this.en = en;
        this.position = position;
    }

    @FXML
    TextArea text;

    @FXML
    Button save, cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        save.setOnAction(event -> {
            controller.update(position, en, text.getText());
            stage.close();
        });
        cancel.setOnAction(event -> stage.close());
        text.setWrapText(true);
    }

    public void setText(String paragraph) {
        text.setText(paragraph);
    }
}
