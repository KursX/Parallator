package com.kursx.parallator.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class AddParagraphsDialogController implements Initializable {

    private Map<String, TextArea> areas = new HashMap<>();

    public void show(Stage stage, MainController controller) {
        stage.show();
        List<String> keys = controller.getKeys();
        for (String key : keys) {
            TextArea textArea = new TextArea();
            areas.put(key, textArea);
            textArea.setPromptText(key);
            editsLayout.getChildren().add(textArea);
        }
        ok.setOnAction(event -> {
            stage.close();
            for (String key : keys) {
                controller.getTextMap().get(key).addAll(Arrays.asList(
                        areas.get(key).getText().split(controller.getConfig().dividerRegex())));
            }
            controller.show();
        });
    }

    @FXML
    HBox editsLayout;

    @FXML
    Button ok;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
