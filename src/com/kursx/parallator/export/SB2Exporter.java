package com.kursx.parallator.export;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.kursx.parallator.*;
import com.kursx.parallator.controller.MainController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class SB2Exporter implements FileExporter {

    @Override
    public File process(MainController controller, Stage rootStage) throws Exception {
        File bookRootFile = controller.getFile();
        if (controller.getBook() == null) {
            Toast.makeText(rootStage, "Заполните информацию о книге (Книга -> Описание)");
            return null;
        }
        File thumbnail = new File(bookRootFile, "thumbnail.jpg");
        if (!thumbnail.exists()) {
            Toast.makeText(rootStage, "Добавьте изображение 300х300");
            return null;
        }
        if (!ImageHelper.checkImageSize(new Image(thumbnail.toURI().toString()))) {
            Toast.makeText(rootStage, "Изображение должно быть 300x300");
            return null;
        }
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
        return sb2RequestToBackend(controller.getBook().getFilename().replace(".sb", ""), bookRootFile);
    }


    public static File sb2RequestToBackend(String bookName, File dir) throws Exception {
        String url = "http://smart-book.net/translation/file";
        String charset = "UTF-8";
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

        // Send normal param.
        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"book_name\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
        writer.append(CRLF).append(bookName).append(CRLF).flush();
        writer.append("--").append(boundary).append(CRLF);

        writer.append("Content-Disposition: form-data; name=\"json\"; filename=\"book.json\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(new File(dir, "book.json").toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
        writer.append("--").append(boundary).append("--").append(CRLF).flush();

        writer.append("Content-Disposition: form-data; name=\"thumbnail\"; filename=\"thumbnail.jpg\"").append(CRLF);
        writer.append("Content-Type: image/jpeg; charset=").append(charset).append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(new File(dir, "thumbnail.jpg").toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
        writer.append("--").append(boundary).append("--").append(CRLF).flush();

        if (new File(dir, "img").exists()) {
            for (File file : new File(dir, "img").listFiles()) {
                writer.append("Content-Disposition: form-data; name=\"" + file.getName() + "\"; filename=\"")
                        .append(file.getName()).append("\"").append(CRLF);
                writer.append("Content-Type: image/jpeg; charset=").append(charset).append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(file.toPath(), output);
                output.flush();
                writer.append(CRLF).flush();
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }
        }

        File result;
        try {
            result = new File(dir, bookName + ".sb2");
            Helper.writeToFile(connection.getInputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            result = new File(dir, bookName + ".html");
            Helper.writeToFile(connection.getErrorStream(), result);
        }
        return result;
    }
}
