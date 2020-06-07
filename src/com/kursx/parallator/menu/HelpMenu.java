package com.kursx.parallator.menu;

import com.kursx.parallator.Logger;
import com.kursx.parallator.Toast;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class HelpMenu {

    public final Menu menu;

    public HelpMenu(Stage stage) {
        menu = new Menu("Помощь");
        MenuItem about = new MenuItem("О Программе");
        MenuItem update = new MenuItem("Обновить программу");
        about.setOnAction(event -> Toast.makeText(stage, "Parallator v1.1 by KursX \n kursxinc@gmail.com", 5000));
        update.setOnAction(event -> update());
        menu.getItems().addAll(update, about);
    }

    public static void update() {
        new Thread(() -> {
            try {
                File file1 = new File("Parallator.jar");
                file1.delete();
                URL website = new URL("https://github.com/KursX/Parallator/raw/master/release/Parallator.jar");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("Parallator.jar");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                System.exit(0);
            } catch (Exception e) {
                Logger.exception(e);
            }
        }).start();
    }
}
