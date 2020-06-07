package com.kursx.parallator;


import com.kursx.parser.fb2.Element;
import com.kursx.parser.fb2.Section;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Helper {

    public static final String CP_1251 = "cp1251", UTF_8 = "utf8";

    public static final String DIV1 = "Перенос строки", DIV2 = "Пустая строка";
    public static final String DIV1REG = "\\n", DIV2REG = "\\n *\\n";

    public static final String[] charsets = {
            CP_1251, UTF_8,
    };

    public static final String[] dividersRegs = {
            DIV1REG, DIV2REG,
    };

    public static final String[] PARAGRAPH_DIVIDERS = {
            DIV1, DIV2,
    };

    public static final String[] DIVIDERS = {
            "\n", "\\.", ".\"", "!", "\\?", "…", ",", ";", ":", "!\"", ".\"", "?\"", "!“", ".“", "?“", "!”", ".”", "?”"
    };

    public static void importFb2(List<Section> sections1, File directory, String from) {
        try {
            write(sections1, directory, from);
        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    public static void write(List<Section> sections, File bookDirectory, String lang) throws IOException {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            File book = new File(bookDirectory + "/" + (i + 1) + "# " + section.getTitleString(". ", ". "));
            for (File file : bookDirectory.listFiles()) {
                if (StringUtils.getBaseName(file).startsWith((i + 1) + "# ")) {
                    book = file;
                    break;
                }
            }
            if (!book.exists()) book.mkdirs();
            if (!section.getSections().isEmpty()) {
                write(section.getSections(), book, lang);
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                if (i < sections.size()) {
                    for (Element p : section.getElements()) {
                        if (p != null && p.getText() != null && !p.getText().isEmpty()) {
                            stringBuilder.append(p.getText());
                            if (p.getText().matches("^.*\\w *$")) {
                                stringBuilder.append(" ");
                            } else {
                                stringBuilder.append("\n");
                            }
                        }
                    }
                }

                List<String> parts = PartsSeparator.getParts(stringBuilder.toString(), false, null);
                StringBuilder resultBuilder = new StringBuilder();
                for (String part : parts) {
                    resultBuilder.append(part).append("\n\n");
                }

                Writer writer1 = new OutputStreamWriter(
                        new FileOutputStream(new File(book, lang + ".txt")), UTF_8);
                writer1.append(resultBuilder);
                writer1.flush();
                writer1.close();
            }
        }
    }

    public static void write(List<Chapter> sections, File directory) throws IOException {
        for (Chapter section : sections) {
            if (section.getChapters() != null) {
                write(section.getChapters(), directory);
            } else {
                File chapter = new File(directory, (sections.indexOf(section) + 1) + "# " + section.getChapterName());
                chapter.mkdir();
                if (section.chapterDescription != null) {
                    File info = new File(chapter, ".description");
                    info.createNewFile();
                    Helper.writeToFile(info, Config.getConfig(directory).enc1(), new StringBuilder(section.chapterDescription));
                }
                Set<String> keys = section.paragraphs.get(0).keySet();
                for (String key : keys) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map<String, String> paragraph : section.paragraphs) {
                        String value = paragraph.get(key);
                        if (value == null) {
                            stringBuilder.append(paragraph.get("img")).append("\n\n");
                        } else {
                            stringBuilder.append(value).append("\n\n");
                        }
                    }
                    FileWriter fileWriter = new FileWriter(new File(chapter, key + ".txt"));
                    fileWriter.append(stringBuilder);
                    fileWriter.flush();
                    fileWriter.close();
                }
            }
        }
    }

    public static String getTextFromFile(String file, String enc) {
        if (!new File(file).exists()) return null;
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), Charset.forName(enc)));
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
            br.close();
            return builder.toString();
        } catch (Exception e) {
            Logger.exception(e);
            return "";
        }
    }

    public static String getTextFromFile(File file, String enc) {
        return getTextFromFile(file.getAbsolutePath(), enc);
    }

    public static File showDirectoryChooser(Scene scene) {
        String home = System.getProperty("user.home");
        File[] downloads = {
                new File(home + "/Downloads/"),
                new File(home + "/Загрузки/"),
                new File(home)
        };
        DirectoryChooser fileChooser = new DirectoryChooser();
        for (File download : downloads) {
            if (download.exists()) {
                fileChooser.setInitialDirectory(download);
                break;
            }
        }
        return fileChooser.showDialog(scene.getWindow());
    }

    public static File showFileChooser(Scene scene, FileChooser.ExtensionFilter filter) {
        String home = System.getProperty("user.home");
        File[] downloads = {
                new File(home + "/Downloads/"),
                new File(home + "/Загрузки/"),
                new File(home)
        };
        FileChooser fileChooser = new FileChooser();
        for (File download : downloads) {
            if (download.exists()) {
                fileChooser.setInitialDirectory(download);
                break;
            }
        }
        fileChooser.setSelectedExtensionFilter(filter);
        return fileChooser.showOpenDialog(scene.getWindow());
    }

    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

        try {
            int size = 1024 * 1024;
            byte[] buf = new byte[size];
            int byteRead;
            while ((byteRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, byteRead);
            }
        } catch (IOException e) {
            Logger.exception(e);
        } finally {
            try {
                outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Logger.exception(e);
            }
        }
    }


    public static void writeToFile(File file, String encoding, StringBuilder builder) throws IOException {
        Writer writer1 = new OutputStreamWriter(new FileOutputStream(file), encoding);
        writer1.append(builder);
        writer1.flush();
        writer1.close();
    }

    public static boolean isDebug() {
        return MainConfig.getMainConfig().isDeveloper();
    }
}
