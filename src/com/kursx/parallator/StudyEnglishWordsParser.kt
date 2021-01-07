package com.kursx.parallator


import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList


object StudyEnglishWords {

    fun parseHtml(html: String): String {
        return html
                .replace("\n", "")
                .replace("<html>.*<body>".toRegex(), "")
                .replace("<br><br>", "\n")
                .replace("<br>", "\n")
                .replace("<small>----------------</small>", "")
                .replace("<small>.*</small>".toRegex(), "")
                .replace("</body></html>", "").trim()
    }


    fun parse(file: File): File {
        val book = Book("", "book", "author", "en", "ru", ArrayList())
        val output = File(file.parent + "/" + book.filename.replace(".sb", ".json"))
        try {
            var text = Helper.getTextFromFile(file.absolutePath, "utf8")
            text = parseHtml(text)
            val strings = text.split("\n\n".toRegex())

            val chapters = ArrayList<Chapter>()
            var paragraphList: MutableList<Map<String, String>> = ArrayList()
            var chapterName = "CHAPTER 1"
            var chapterDescription: String? = null
            var i = 0
            while (i < strings.size - 1) {
                val from = strings[i++].trim()
                val to = strings[i++].trim()
                if (from.contains("CHAPTER")) {

                    if (paragraphList.isNotEmpty()) {
                        chapters.add(Chapter(chapterName, chapterDescription, paragraphList))
                        paragraphList = ArrayList()
                    }
                    chapterName = from.split(" ")[0] + " " + from.split(" ")[1]
                    chapterDescription = from.split(" ").apply {
                        drop(2)
                    }.joinToString(" ")
                    paragraphList.add(mapOf("en" to from, "ru" to to))
                } else {
                    paragraphList.add(mapOf("en" to from, "ru" to to))
                }
            }
            chapters.add(Chapter(chapterName, chapterDescription, paragraphList))
            book.chapters = chapters
            val wrt = FileWriter(output)
            wrt.append(Gson().toJson(book))
            wrt.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return output
    }
}

