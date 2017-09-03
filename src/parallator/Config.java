package parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    public static String CONFIG = ".config";
    private String file;
    private String enc1 = Helper.UTF_8, enc2 = Helper.UTF_8, divider = Helper.DIV2REG;
    private int lastChapter = 0;
    private Book book;

    public Config(File file) {
        this.file = new File(file, CONFIG).getAbsolutePath();
    }

    public void save() {
        String text = Security.encrypt(new Gson().toJson(this));
        try {
            FileWriter wrt = new FileWriter(new File(file));
            wrt.append(text);
            wrt.flush();
            wrt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig(File file) {
        if (file == null) return null;
        String text = Helper.getTextFromFile(new File(file, CONFIG), Helper.UTF_8);
        Config config = null;
        try {
            text.isEmpty();
            config = new Gson().fromJson(Security.decrypt(text), Config.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config == null) {
            config = new Config(file);
            config.save();
        }
        if (config.file() == null) config.setFile(new File(file, CONFIG).getAbsolutePath());
        return config;
    }

    public void setLastChapter(int lastChapter) {
        this.lastChapter = lastChapter;
        save();
    }

    public int getLastChapter() {
        return lastChapter;
    }

    public String enc1() {
        return enc1;
    }

    public void setEnc1(String enc1) {
        this.enc1 = enc1;
        save();
    }

    public String enc2() {
        return enc2;
    }

    public void setEnc2(String enc2) {
        this.enc2 = enc2;
        save();
    }

    public String divider() {
        return divider;
    }

    public void setDivider(String divider) {
        this.divider = divider;
        save();
    }

    public void setFile(String file) {
        this.file = file;
        save();
    }

    public String file() {
        return file;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        save();
    }
}
