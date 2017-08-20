package parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private String file;
    private String enc1 = "utf8", enc2 = "Cp1251", divider = Helper.DIV2REG;
    public long time = 0;
    private int lastChapter = 0;

    public Config(File file) {
        this.file = new File(file, "config").getAbsolutePath();
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
}
