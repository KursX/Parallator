package parallator;


import java.util.ArrayList;
import java.util.List;

public class Book {

    class Lang {
        String name = "";
        String lang = "";
        String author = "";

        public Lang(String lang, String name,  String author) {
            this.name = name;
            this.lang = lang;
            this.author = author;
        }
    }

    private String lang = "en";
    private String name;
    private String rusName;
    private String author;
    private String enAuthor;
    private String hash = "";
    private String thumbnail = "";
    private String filename = "";
    private Lang[] langs;
    private List<Chapter> chapters;

    public Book(String name, String rusName, String author,  String authorEn,  String from, String to, List<Chapter> chapters) {
        this.name = name;
        this.lang = from + "-" + to;
        this.rusName = rusName;
        this.chapters = chapters;
        this.author = author;
        this.enAuthor = authorEn;
        langs = new Lang[] {
                new Lang("en", name, authorEn),
                new Lang("ru", rusName, author),
                new Lang("de", name, null),
                new Lang("es", name, null),
                new Lang("fr", name, null),
                new Lang("tr", name, null),
                new Lang("uk", rusName, author),
                new Lang("bg", rusName, author)
        };
        for (String part : name.split(" ")) {
            filename += part.toLowerCase() + "_";
        }
        filename = filename.substring(0, filename.length() - 1) + ".sb";
    }

    public Book(String name, String rusName, String author,  String authorEn,  String from, String to, String thumbnail) {
        this(name, rusName, author, authorEn, from, to, new ArrayList<>());
        this.thumbnail = thumbnail;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public String getLang() {
        return lang;
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

    public Lang[] getLangs() {
        return langs;
    }

    public String getEnAuthor() {
        return enAuthor;
    }
}
