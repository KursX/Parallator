package com.kursx.parallator


import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.util.*


object StudyEnglishWords {


    fun parse(file: File) {
        try {
            var text = Helper.getTextFromFile(file.absolutePath, "utf8")
            text = text.replace("----------------", "");
            text = text.replace("\\[Параграф №\\d+]".toRegex(), "");
            val strings = text.split("\n\n".toRegex())

            val chapters = ArrayList<Chapter>()
            var paragraphList: MutableList<Map<String, String>> = ArrayList()
            var paragraph: Map<String, String>?
            var chapterName = ""
            val chapterDescription: String? = null
            var i = 0
            while (i < strings.size - 1) {
                val from = strings[i++].trim()
                val to = strings[i++].trim()
                if (from.startsWith("CHAPTER")) {

                    if (paragraphList.isNotEmpty()) {
                        chapters.add(Chapter(chapterName, chapterDescription, paragraphList))
                        paragraphList = ArrayList()
                    }
                    chapterName = from
                } else {
                    paragraph = HashMap()
                    paragraph["en"] = from
                    paragraph["ru"] = to
                    paragraphList.add(paragraph)
                }
            }
            chapters.add(Chapter(chapterName, chapterDescription, paragraphList))
            val book = Book("", "book", "author", "en", "ru", chapters)
            val wrt = FileWriter(File(file.parent + "/" + book.filename.replace(".sb", ".json")))
            wrt.append(Gson().toJson(book))
            wrt.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

