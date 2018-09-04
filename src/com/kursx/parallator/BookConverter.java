package com.kursx.parallator;

import com.google.gson.Gson;
import javafx.stage.Stage;
import com.kursx.parallator.controller.MainController;
import com.kursx.parallator.controller.ValidationException;
import com.kursx.parallator.export.FileExporter;
import com.kursx.parallator.factrory.ChapterCellFactory;

import java.io.*;

public class BookConverter {

    public static boolean convert(MainController controller, Stage rootStage, FileExporter exporter) {
        File result = null;
        try {
            result = exporter.process(controller, rootStage);
            Toast.makeText(rootStage, "Файл сохранен\n" + result.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(rootStage, "Произошла ошибка");
            Logger.exception(e);
        }
        return result != null;
    }

    public static Book saveDirectoriesToJsonFile(MainController controller) throws IOException, ValidationException {
        Book book = controller.getBook();
        if (book == null) return null;
        PrintWriter printWriter = new PrintWriter(new File(controller.getFile(), "book.json"));
        book.setChapters(ChapterCellFactory.getChapters(controller.getFile(), controller));
        printWriter.print(new Gson().toJson(book));
        printWriter.flush();
        printWriter.close();
        return book;
    }
}
