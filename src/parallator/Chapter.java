package parallator;


import java.util.List;

public class Chapter {

    public String hash;
    public String chapterName;
    String chapterDescription;
    List<Paragraph> paragraphs;

    public Chapter(String chapterName, String chapterDescription, List<Paragraph> paragraphs) {
        this.chapterName = chapterName;
        this.paragraphs = paragraphs;
        this.chapterDescription = chapterDescription;
    }

    public Chapter(String chapterName) {
        this.chapterName = chapterName;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getChapterName() {
        return chapterName;
    }
}
