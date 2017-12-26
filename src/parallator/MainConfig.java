package parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainConfig {

    private List<String> pathes = new ArrayList<>();
    private int fontSize = 15;
    public final ArrayList<String> dividers = new ArrayList<>();

    public MainConfig() {
    }

    public void save() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        try {
            FileWriter wrt = new FileWriter(new File(workingDirectory, ".config"));
            wrt.append(new Gson().toJson(this));
            wrt.flush();
            wrt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String path() {
        if (pathes == null) return null;
        for (String path : pathes) {
            if (!new File(path).exists()) {
                pathes.remove(new File(path));
            }
        }
        if (pathes.isEmpty()) return null;
        return pathes.get(0);
    }

    public void setBookPath(String path) {
        if (pathes.contains(path)) {
            pathes.remove(path);
        }
        pathes.add(0, path);
        new ArrayList<>(pathes).stream().filter(file -> !new File(file).exists()).forEachOrdered(file -> pathes.remove(file));
        save();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        save();
    }

    public List<String> getPathes() {
        new ArrayList<>(pathes).stream().filter(file -> !new File(file).exists()).forEachOrdered(file -> pathes.remove(file));
        return pathes == null ? new ArrayList<>() : pathes;
    }

    public static MainConfig getMainConfig() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        File file = new File(workingDirectory, ".config");
        String text = Helper.getTextFromFile(file, Helper.UTF_8);
        if (text == null) {
            MainConfig config = new MainConfig();
            config.save();
            return config;
        }
        return new Gson().fromJson(text, MainConfig.class);
    }
}
