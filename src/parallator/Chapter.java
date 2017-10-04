package parallator;


import java.util.List;
import java.util.Map;

public class Chapter {

    public String hash;
    public String chapterName;
    String chapterDescription;
    List<Map<String, String>> paragraphs;
    List<Chapter> chapters;

    public Chapter(String chapterName, String chapterDescription, List<Map<String, String>> paragraphs) {
        this.chapterName = chapterName;
        this.paragraphs = paragraphs;
        this.chapterDescription = chapterDescription;
    }

    public Chapter(String chapterName, List<Chapter> chapters) {
        this.chapterName = chapterName;
        this.chapters = chapters;
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

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
