package com.kursx.parallator.menu;

import com.kursx.parallator.Config;
import com.kursx.parallator.Helper;
import com.kursx.parallator.Logger;
import com.kursx.parallator.MainConfig;
import com.kursx.parallator.controller.MainController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class FileMenu {

    public final Menu menu;

    public FileMenu(MainConfig mainConfig, Stage rootStage, MainController rootController) {
        menu = new Menu("Файл");
        Menu create = new Menu("Создать");
        Menu enc1 = new Menu("Кодировка");
        Menu last = new Menu("Последние книги");
        Menu divider = new Menu("Разделитель абзацев");

        MenuItem open = new MenuItem("Открыть");
        MenuItem save = new MenuItem("Сохранить");
        MenuItem undo = new MenuItem("Отменить");
        MenuItem refresh = new MenuItem("Обновить текст");

        open.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        undo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        refresh.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));

        undo.setOnAction(event -> rootController.undo());


        for (int i = 0; i < Helper.PARAGRAPH_DIVIDERS.length; i++) {
            MenuItem item = new MenuItem(Helper.PARAGRAPH_DIVIDERS[i]);
            divider.getItems().add(item);
            final int index = i;
            item.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setDivider(Helper.dividersRegs[index], rootController.getFile());
                rootController.showChapter();
            });
        }

        mainConfig.getPathes().stream().filter(path -> new File(path).exists()).forEach(path -> {
            MenuItem item = new MenuItem(path);
            last.getItems().add(item);
            item.setOnAction(event -> {
                mainConfig.setBookPath(path);
                rootController.open(new File(path));
            });
        });

        for (String charset : Helper.charsets) {
            MenuItem e1 = new MenuItem(charset);
            enc1.getItems().add(e1);
            e1.setOnAction(event -> {
                if (rootController.getFile() == null) return;
                Config.getConfig(rootController.getFile()).setEnc1(e1.getText(), rootController.getFile());
                rootController.showChapter();
            });
        }

        if (mainConfig.dividers.isEmpty()) {
            Collections.addAll(mainConfig.dividers, "\\.", "!", "\\?", "…");
            mainConfig.save();
        }
        open.setOnAction(event -> {
            File bookFile = Helper.showDirectoryChooser(rootStage.getScene());
            if (bookFile != null) {
                mainConfig.setBookPath(bookFile.getAbsolutePath());
                rootController.open(bookFile);
            }
        });
        save.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            rootController.save();
        });
        refresh.setOnAction(event -> {
            if (rootController.getFile() == null) return;
            rootController.showChapter();
        });


        menu.getItems().addAll(create, undo, open, save, refresh, last, enc1, divider);


        create.setOnAction(event -> {
            File bookFile = Helper.showDirectoryChooser(rootStage.getScene());
            if (bookFile != null) {
                File firstChapter = new File(bookFile, "1# Chapter 1");
                firstChapter.mkdir();
                File firstFile = new File(firstChapter, "en.txt");
                File secondFile = new File(firstChapter, "ru.txt");
                try {
                    firstFile.createNewFile();
                    secondFile.createNewFile();
                    Helper.writeToFile(firstFile, Config.getConfig(bookFile).enc1(), new StringBuilder("English text. Let's start"));
                    Helper.writeToFile(secondFile, Config.getConfig(bookFile).enc1(), new StringBuilder("Русский текст. Начинаем"));
                } catch (IOException e) {
                    Logger.exception(e);
                }
                rootController.open(firstChapter);
                rootController.showChapter(firstChapter);
            }
        });
    }
}
