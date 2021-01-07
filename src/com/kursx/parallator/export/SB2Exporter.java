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
            Toast.makeText(rootStage, "Добавьте изображение");
            return null;
        }
        if (!ImageHelper.checkImageSize(new Image(thumbnail.toURI().toString()))) {
            Toast.makeText(rootStage, "Изображение должно быть квадратным");
            return null;
        }
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
        return sb2RequestToBackend(controller.getBook().getFilename().replace(".sb", ""), bookRootFile);
    }


    static String root = "/home/sb2/";
    static String url = "https://smart-book.net/sb2/";

//    static String root = "/Users/macbook/";
//    static String url = "http://localhost:8080/";

    public static File sb2RequestToBackend(String bookName, File dir) throws Exception {
        uploadFile(bookName, new File(dir, "book.json"), bookName + ".json");
        uploadFile(bookName, new File(dir, "thumbnail.jpg"), bookName + ".jpg");

        HttpURLConnection connection = (HttpURLConnection) new URL(url + "/export.sb2?filename=" + bookName).openConnection();

        File result;
        try {
            result = new File(dir, bookName + ".sb2");
            Helper.writeToFile(connection.getInputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            result = new File(dir, bookName + ".html");
            Helper.writeToFile(connection.getErrorStream(), result);
        }
        return dir;
    }


    public static void uploadFile(String bookName, File file, String name) throws Exception {
        String charset = "UTF-8";
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        HttpURLConnection connection = (HttpURLConnection) new URL(url + "upload").openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; filename=\"").append(name).append("\"; path=\"").append(root).append(bookName).append(CRLF);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
        writer.append("--").append(boundary).append("--").append(CRLF).flush();

        connection.getInputStream();
    }
}
