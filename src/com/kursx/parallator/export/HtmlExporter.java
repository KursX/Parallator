package com.kursx.parallator.export;

import javafx.stage.Stage;
import com.kursx.parallator.Book;
import com.kursx.parallator.BookConverter;
import com.kursx.parallator.Chapter;
import com.kursx.parallator.Helper;
import com.kursx.parallator.controller.BookDialogController;
import com.kursx.parallator.controller.MainController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HtmlExporter implements FileExporter {

    @Override
    public File process(MainController controller, Stage rootStage) throws Exception {
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
        return writeHtml(controller.getTextMap(), controller.getFile(), book.getOnlyChaptersWithParagraphs(null, ""),
                controller.getConfig().enc1());
    }


    public static File writeHtml(Map<String, List<String>> map, final File path, List<Chapter> list, String encoding) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");

        for (Chapter chapter : list) {
            builder.append(chapter.chapterName);
            builder.append("<table cellspacing = \"0\" border = \"1\" style=\"width:100%;\">");
            builder.append("<tr>");
            int width = 100 / map.keySet().size();
            for (String key : map.keySet()) {
                builder.append("<td width=\"").append(width).append("%\">").append(key).append("</td>");
            }
            builder.append("</tr>");

            int position = 0;
            while (position < chapter.paragraphs.size()) {
                builder.append("<tr>");
                for (String key : map.keySet()) {
                    builder.append("\n<td width=\"").append(width).append("%\">");
                    String value = chapter.paragraphs.get(position).get(key);
                    builder.append(value);
                    builder.append("</td>");
                }
                builder.append("</tr>");
                position++;
            }
            builder.append("</table>");
        }

        builder.append("</body></html>");
        path.mkdir();
        File file = new File(path, "book.html");
        Helper.writeToFile(file, encoding, builder);
        return file;
    }
}
