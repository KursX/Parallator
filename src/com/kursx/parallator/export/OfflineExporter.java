package com.kursx.parallator.export;

import com.google.gson.Gson;
import com.kursx.parallator.Book;
import com.kursx.parallator.BookConverter;
import com.kursx.parallator.Chapter;
import com.kursx.parallator.Helper;
import com.kursx.parallator.controller.MainController;
import javafx.stage.Stage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class OfflineExporter implements FileExporter {

    @Override
    public File process(MainController controller, Stage rootStage) throws Exception {
        Book book = BookConverter.saveDirectoriesToJsonFile(controller);
        if (book == null) return null;
        return csv(controller.getFile(), book.getOnlyChaptersWithParagraphs(null, ""),
                controller.getConfig().enc1());
    }

    private static String salt = "";

    public static File csv(final File path, List<Chapter> list, String encoding) throws Exception {

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
