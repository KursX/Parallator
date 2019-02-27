package com.kursx.parallator.export;

import javafx.stage.Stage;
import com.kursx.parallator.*;
import com.kursx.parallator.controller.BookDialogController;
import com.kursx.parallator.controller.MainController;

import java.io.*;
import java.util.List;
import java.util.Map;

public class CSVExporter implements FileExporter {

    @Override
    public File process(MainController controller, Stage rootStage) throws Exception {
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
        return csv(controller.getTextMap(), controller.getFile(), book.getOnlyChaptersWithParagraphs(null, ""),
                controller.getConfig().enc1());
    }

    public static File csv(Map<String, List<String>> map, final File path, List<Chapter> list, String encoding) throws Exception {
        StringBuilder builder = new StringBuilder();

        for (Chapter chapter : list) {
            builder.append(chapter.chapterName.replace(";", ""));
            builder.append("\n");
            for (String key : map.keySet()) {
                builder.append(key).append(";");
            }
            builder.append("\n");

            int position = 0;
            while (position < chapter.paragraphs.size()) {
                for (String key : map.keySet()) {
                    String value = chapter.paragraphs.get(position).get(key);
                    builder.append(value.replace(";", "")).append(";");
                }
                builder.append("\n");
                position++;
            }
        }

        path.mkdir();
        File file = new File(path, "book.csv");
        Helper.writeToFile(file, encoding, builder);
        return file;
    }
}
