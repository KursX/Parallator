package parallator;


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

    String lang = "en";
    String name;
    String rusName;
    String author;
    String hash = "";
    String thumbnail = "";
    public String filename = "";
    int size;
    Lang[] langs;

    public List<Chapter> chapters;

    public Book(String name, String rusName, String author,  String authorEn,  String lang) {
        this.name = name;
        this.lang = lang;
        this.rusName = rusName;
        this.author = author;
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

    public List<Chapter> getChapters() {
        return chapters;
    }
}
