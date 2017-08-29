package parallator;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainConfig {

    private String path;

    public MainConfig() {
    }

    public void save() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        try {
            FileWriter wrt = new FileWriter(new File(workingDirectory, "config"));
            wrt.append(new Gson().toJson(this));
            wrt.flush();
            wrt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String path() {
        return path;
    }

    public void setBookPath(String path) {
        this.path = path;
        save();
    }
}
