package parallator;


import com.google.gson.Gson;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

public class Helper {

    public static final String CP_1251 = "cp1251", UTF_8 = "utf8";

    public static final String DIV1 = "Перенос строки", DIV2 = "Пустая строка";
    public static final String DIV1REG = "\\n", DIV2REG = "\\n *\\n";

    public static final String[] charsets = {
            CP_1251, UTF_8,
    };

    public static final String[] dividersRegs = {
            DIV1REG, DIV2REG,
    };

    public static final String[] dividers = {
            DIV1, DIV2,
    };

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
                e.printStackTrace();
            }
        }).start();
    }

    public static String getTextFromFile(String file, String enc) {
        if (!new File(file).exists()) return null;
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), Charset.forName(enc)));
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
            br.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTextFromFile(File file, String enc) {
        return getTextFromFile(file.getAbsolutePath(), enc);
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

    public static Config getConfig(File file) {
        String text = getTextFromFile(new File(file, "config"), UTF_8);
        if (text == null) {
            Config config = new Config(file);
            config.save();
            return config;
        }
        Config config = new Gson().fromJson(Security.decrypt(text), Config.class);
        if (config.file() == null) config.setFile(new File(file, "config").getAbsolutePath());
        return config;
    }

    public static MainConfig getMainConfig() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        File file = new File(workingDirectory, "config");
        String text = getTextFromFile(file, UTF_8);
        if (text == null) {
            MainConfig config = new MainConfig(file);
            config.save();
            return config;
        }
        return new Gson().fromJson(text, MainConfig.class);
    }
}
