package com.kursx.parallator;

import com.kursx.parallator.menu.BookMenu;
import com.kursx.parallator.menu.FileMenu;
import com.kursx.parallator.menu.HelpMenu;
import com.kursx.parallator.menu.ViewMenu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.kursx.parallator.controller.MainController;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class Main extends Application {

    private MainController rootController;
    private static Main main;

    private ViewMenu viewMenu;
    private HelpMenu helpMenu;
    private BookMenu bookMenu;
    private FileMenu fileMenu;

    public static Main getMain() {
        return main;
    }

    @Override
    public void start(Stage rootStage) throws Exception {
        rootStage.setMaximized(true);
        main = this;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layouts/main.fxml"));

        Parent root = loader.load();

        Scene rootScene = new Scene(root);

        rootStage.setTitle("Parallator");

        rootController = loader.getController();

        rootStage.setScene(rootScene);

        initMenu(rootStage);

        rootStage.show();
        rootStage.setOnCloseRequest(event -> {
            if (rootController.getFile() == null || !rootController.isEdited()) return;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Сохранить перед выходом?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                rootController.save();
            }
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
            }
        });

        rootScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DOWN:
                    rootController.down();
                    break;
                case UP:
                    rootController.up();
                    break;
            }
        });

        MainConfig config = MainConfig.getMainConfig();
        if (config.path() != null && new File(config.path()).exists()) {
            rootController.open(new File(config.path()));
        }
    }

    public void initMenu(Stage rootStage) throws IOException {
        MainConfig mainConfig = MainConfig.getMainConfig();

        MenuBar menuBar = new MenuBar();
        fileMenu = new FileMenu(mainConfig, rootStage, rootController);
        viewMenu = new ViewMenu(mainConfig, rootController);
        helpMenu = new HelpMenu(rootStage);
        bookMenu = new BookMenu(rootController, rootStage, this);

        menuBar.getMenus().addAll(fileMenu.menu, bookMenu.menu, helpMenu.menu, viewMenu.menu);

        ((VBox) rootStage.getScene().getRoot()).getChildren().add(0, menuBar);
    }

    public MainController getRootController() {
        return rootController;
    }

    public Menu getLangs() {
        return viewMenu.langs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
