package com.kursx.parallator.export;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kursx.parallator.Book;
import com.kursx.parallator.BookConverter;
import com.kursx.parallator.Chapter;
import com.kursx.parallator.Helper;
import com.kursx.parallator.controller.MainController;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class OfflineExporter implements FileExporter {

    @Override
    public File process(MainController controller, Stage rootStage) throws Exception {
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
//        return null;
        return offline(controller.getFile(), book.getOnlyChaptersWithParagraphs(null, ""),
                controller.getConfig().enc1());
//        return offlineFromOnline(controller, controller.getFile(), book.getOnlyChaptersWithParagraphs(null, ""),
//                controller.getConfig().enc1());
    }

    private static String salt = "sbsecret";

    public static File offline(final File path, List<Chapter> list, String encoding) throws Exception {

        Map<String, String> header = new HashMap<>();
        header.put("version", "2.1");
        header.put("type", "apt");

        for (Chapter chapter : list) {

            Map<String, String> translation = new HashMap<>();
            for (int i = 0; i < chapter.paragraphs.size(); i++) {
                byte bytes[] = (salt + chapter.paragraphs.get(i).get("en")).getBytes();
                String checksumValue = encrypt(bytes).substring(0, 10);

                translation.put(checksumValue, chapter.paragraphs.get(i).get("ru"));
                header.put(checksumValue, chapter.path);
            }
            new File(path, "pt").mkdir();
            File file = new File(path, "pt/" + chapter.path + ".pt");
            file.createNewFile();
            Helper.writeToFile(file, encoding, new StringBuilder(new Gson().toJson(translation)));
        }
        File file = new File(path, "pt/header.pt");
        file.createNewFile();
        Helper.writeToFile(file, encoding, new StringBuilder(new Gson().toJson(header)));

        return new File(path, "pt");
    }

    public static File offlineFromOnline(MainController rootController, final File path, List<Chapter> list, String encoding) throws Exception {
        new File(path, "pt").mkdirs();
        Map<String, String> header = new HashMap<>();
        header.put("version", "2.1");
        header.put("type", "apt");

        float count = 0;
        float counter = 0;

        for (Chapter chapter : list) {
            count += chapter.paragraphs.size();
        }

        for (Chapter chapter : list) {

            Map<String, String> translation = new HashMap<>();
            for (int i = 0; i < chapter.paragraphs.size(); i++) {
                rootController.startProgress("Подождите, идет создание offline " + (counter++ * 100 / count));

                String en = chapter.paragraphs.get(i).get("en");
                byte bytes[] = (salt + en).getBytes();
                String checksumValue = encrypt(bytes).substring(0, 10);

                URL url = new URL("http://smart-book.net/translation/translate?lang=en-ru&text=" + URLEncoder.encode(en));
                URLConnection con = url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                StringBuilder response = new StringBuilder();
                String currentLine;

                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine);

                in.close();

                String translated = new Gson().fromJson(response.toString(), JsonObject.class).get("text").getAsString();
                translation.put(checksumValue, translated);
                header.put(checksumValue, chapter.path);
            }

            File file = new File(path, "pt/" + chapter.path + ".pt");
            file.createNewFile();
            Helper.writeToFile(file, encoding, new StringBuilder(new Gson().toJson(translation)));
        }
        File file = new File(path, "pt/header.pt");
        file.createNewFile();
        Helper.writeToFile(file, encoding, new StringBuilder(new Gson().toJson(header)));

        return new File(path, "pt");
    }

    private static String encrypt(byte text[] )
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(text);
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
