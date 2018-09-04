package com.kursx.parallator.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EditDialogController implements Initializable {

    private Stage stage;
    private Runnable handler;

    public void init(Stage stage,Runnable handler) {
        this.stage = stage;
        this.handler = handler;
    }

    @FXML
    TextArea text;

    @FXML
    Button save, cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        save.setOnAction(event -> handler.run());
        cancel.setOnAction(event -> stage.close());
        text.setWrapText(true);
    }

    public void close() {
        stage.close();
    }

    public void setText(String paragraph) {
        text.setText(paragraph);
    }

    public String getText() {
        return text.getText();
    }
}
