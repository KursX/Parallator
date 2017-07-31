package parallator;


import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Helper {

    public static String getTextFromFile(String file) {
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static File showDirectoryChooser(Scene scene) {
        String home = System.getProperty("user.home");
        File[] downloads = {
                new File(home + "/Downloads/"),
                new File(home + "/Загрузки/"),
                new File(home)
        };
        DirectoryChooser fileChooser = new DirectoryChooser();
        for (File download : downloads) {
            if (download.exists()) {
                fileChooser.setInitialDirectory(download);
                break;
            }
        }
        return fileChooser.showDialog(scene.getWindow());
    }
}
