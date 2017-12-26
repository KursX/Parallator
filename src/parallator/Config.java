package parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Config {

    public static String CONFIG = ".config";
    private String enc1 = Helper.UTF_8, divider = Helper.DIV2REG;
    private String lastChapterPath;
    private Book book;
    public String filePath;
    private boolean red;

    public Config() {
    }

    public void save() {
        String text = Security.encrypt(new Gson().toJson(this));
        try {
            FileWriter wrt = new FileWriter(filePath);
            wrt.append(text);
            wrt.flush();
            wrt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig(File directory) {
        if (directory == null) return null;
        File file = new File(directory, CONFIG);
        String text = Helper.getTextFromFile(file, Helper.UTF_8);
        Config config = null;
        if (text != null) {
            try {
                config = new Gson().fromJson(Security.decrypt(text), Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (config == null) {
            config = new Config();
            config.filePath = file.getAbsolutePath();
            config.save();
        }
        config.filePath = file.getAbsolutePath();
        return config;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapterPath = lastChapter;
        save();
    }

    public File getLastChapter() {
        if (lastChapterPath == null) return null;
        File file = new File(lastChapterPath);
        if (file.exists()) {
            return file;
        }
        setLastChapter(lastChapterPath);
        return null;
    }

    public String enc1() {
        return enc1;
    }

    public void setEnc1(String enc1) {
        this.enc1 = enc1;
        save();
    }

    public String dividerRegex() {
        return divider;
    }

    public String divider() {
        return divider.replace(".", "").replace("*", "").replace(" ", "").replace("\\n", ((char) 10) + "");
    }

    public void setDivider(String divider) {
        this.divider = divider;
        save();
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        save();
    }

    public void setRed(boolean red) {
        this.red = red;
        save();
    }

    public boolean isRed() {
        return red;
    }
}
