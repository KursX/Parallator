package com.kursx.parallator;


import java.util.List;
import java.util.Map;

public class Chapter {

    public String chapterName;
    public String chapterDescription;
    public List<Map<String, String>> paragraphs;
    List<Chapter> chapters;

    public Chapter(String chapterName, String chapterDescription, List<Map<String, String>> paragraphs) {
        this.chapterName = chapterName;
        this.paragraphs = paragraphs;
        if (chapterDescription != null) {
            this.chapterDescription = chapterDescription.trim();
        }
    }

    public Chapter(String chapterName, List<Chapter> chapters) {
        this.chapterName = chapterName;
        this.chapters = chapters;
    }

    public String getChapterName() {
        return chapterName;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}
