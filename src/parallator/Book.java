package parallator;


import java.util.ArrayList;
import java.util.List;

public class Book {

    public static class Lang {
        public String name = "";
        public String lang = "";
        public String author = "";

        public Lang(String lang, String name,  String author) {
            this.name = name;
            this.lang = lang;
            this.author = author;
        }
    }

    private String direction = "en-ru";
    private String version = "1.9";
    private String name;
    private String rusName;
    private String author;
    private String hash = "";
    private String thumbnail = "";
    private String filename = "";
    private int size;
    private List<Lang> langs = new ArrayList<>();
    private List<Chapter> chapters;

    public Book(String name, String rusName, String author,  String from, String to, List<Chapter> chapters) {
        this.name = name;
        this.direction = from + "-" + to;
        this.rusName = rusName;
        this.chapters = chapters;
        this.author = author;
        langs.add(new Lang(from, name, author));
        for (String part : name.split(" ")) {
            filename += part.toLowerCase() + "_";
        }
        filename = filename.substring(0, filename.length() - 1) + ".sb";
    }

    public Book(String name, String rusName, String author,  String from, String to, String thumbnail) {
        this(name, rusName, author, from, to, new ArrayList<>());
        this.thumbnail = thumbnail;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public String getLang() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public String getRusName() {
        return rusName;
    }

    public String getAuthor() {
        return author;
    }

    public String getHash() {
        return hash;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getFilename() {
        return filename;
    }

    public List<Lang> getLangs() {
        return langs;
    }
}
