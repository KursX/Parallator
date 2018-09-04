package com.kursx.parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static String CONFIG = ".conf";
    private String enc1 = Helper.UTF_8, divider = Helper.DIV2REG;
    private String lastChapterPath;
    private boolean red = true;
    private Map<String, Integer> bookmarks;

    public Config() {
    }

    public void save(File directory) {
        String text = new Gson().toJson(this);
        try {
            File file = new File(directory, CONFIG);
            if (file.exists() || file.createNewFile()) {
                FileWriter wrt = new FileWriter(file);
                wrt.append(text);
                wrt.flush();
                wrt.close();
            }
        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    public static Config getConfig(File directory) {
        if (directory == null) return null;
        File file = new File(directory, CONFIG);
        String text = Helper.getTextFromFile(file, Helper.UTF_8);
        Config config = null;
        if (text != null) {
            try {
                config = new Gson().fromJson(text, Config.class);
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        if (config == null) {
            config = new Config();
            config.save(directory);
        }
        return config;
    }

    public void setLastChapter(String lastChapter, File file) {
        this.lastChapterPath = lastChapter;
        save(file);
    }

    public File getLastChapter(File bookFile) {
        if (lastChapterPath == null) return null;
        File file = new File(lastChapterPath);
        if (file.exists()) {
            return file;
        }
        setLastChapter(lastChapterPath, bookFile);
        return null;
    }

    public String enc1() {
        return enc1;
    }

    public void setEnc1(String enc1, File file) {
        this.enc1 = enc1;
        save(file);
    }

    public String dividerRegex() {
        return divider;
    }

    public String divider() {
        return divider.replace(".", "").replace("*", "").replace(" ", "").replace("\\n", ((char) 10) + "");
    }

    public void setDivider(String divider, File file) {
        this.divider = divider;
        save(file);
    }

    public void setRed(boolean red, File file) {
        this.red = red;
        save(file);
    }

    public void addBookmark(int position, File file) {
        if (bookmarks == null) bookmarks = new HashMap<>();
        bookmarks.put(lastChapterPath, position);
        save(file);
    }

    public int getBookmark() {
        if (bookmarks == null || bookmarks.get(lastChapterPath)== null ) return 0;
        return bookmarks.get(lastChapterPath);
    }

    public boolean isRed() {
        return red;
    }
}
