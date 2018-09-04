package com.kursx.parallator.menu;

import com.kursx.parallator.Helper;
import com.kursx.parallator.MainConfig;
import com.kursx.parallator.controller.MainController;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;

public class ViewMenu {

    public final Menu menu, langs;

    public ViewMenu(MainConfig mainConfig, MainController rootController) {
        menu = new Menu("Вид");
        langs = new Menu("Языки");
        Menu font = new Menu("Размер текста");
        Menu dividers = new Menu("Разделители");
        CheckMenuItem developer = new CheckMenuItem("Режим разработчика");
        developer.setOnAction(event -> {
            mainConfig.setDeveloper(developer.isSelected());
        });


        menu.getItems().addAll(font, langs, dividers, developer);


        for (int index = 0; index < Helper.DIVIDERS.length; index++) {
            String div = Helper.DIVIDERS[index];
            final CheckMenuItem item = new CheckMenuItem((div.equals("\n") ? "Перенос строки" : div).replace("\\", ""));
            if (mainConfig.dividers.contains(div)) {
                item.setSelected(true);
            }
            dividers.getItems().add(item);
            item.setOnAction(event -> {
                if (item.isSelected()) {
                    mainConfig.dividers.add(div);
                } else {
                    mainConfig.dividers.remove(div);
                }
                mainConfig.save();
            });
        }

        for (int fontSize = 5; fontSize <= 30; fontSize++) {
            final CheckMenuItem item = new CheckMenuItem(fontSize + "");
            if (mainConfig.getFontSize() == fontSize) {
                item.setSelected(true);
            }
            final int finalValue = fontSize;
            item.setOnAction(event -> {
                ((CheckMenuItem) font.getItems().get(mainConfig.getFontSize() - 5)).setSelected(false);
                mainConfig.setFontSize(finalValue);
                item.setSelected(true);
                rootController.redraw();
            });
            font.getItems().addAll(item);
        }
    }
}
