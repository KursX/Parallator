package com.kursx.parallator.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TextDialogController implements Initializable {

    private Stage stage;

    public void show(Stage stage, String data) {
        this.stage = stage;
        text.setText(data);
        stage.show();
    }

    @FXML
    Label text;

    @FXML
    Button ok;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ok.setOnAction(event -> stage.close());
        text.setWrapText(true);
    }

    public void setText(String paragraph) {
        text.setText(paragraph);
    }

    public String getText() {
        return text.getText();
    }
}
