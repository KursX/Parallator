package parallator;


import com.kursx.parser.fb2.Element;
import com.kursx.parser.fb2.Section;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import parallator.controller.MainController;

import javax.xml.bind.ValidationException;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
            "\n", "\\.", ".\"", ".”", "!", "\\?", "…", ",", ";", ":"
    };

    public static void importFb2(List<Section> sections1, File directory, String from) {
        try {
            write(sections1, directory, from);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(List<Section> sections, File bookDirectory, String lang) throws IOException {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            File book = new File(bookDirectory + "/" + (i + 1) + "# " + section.getTitleString(". ", ". ") + "Chapter " + (i + 1));
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
                StringBuilder stringBuilder1 = new StringBuilder();
                if (i < sections.size()) {
                    for (Element p : section.getElements()) {
                        if (p != null && p.getText() != null && !p.getText().isEmpty()) {
                            stringBuilder1.append(p.getText()).append("\n\n");
                        }
                    }
                }
                Writer writer1 = new OutputStreamWriter(
                        new FileOutputStream(new File(book, lang + ".txt")), UTF_8);
                writer1.append(stringBuilder1);
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
                StringBuilder stringBuilder1 = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                for (Map<String, String> paragraph : section.paragraphs) {
                    stringBuilder1.append(paragraph.get("en")).append("\n\n");
                    stringBuilder2.append(paragraph.get("ru")).append("\n\n");
                }
                FileWriter fileWriter1 = new FileWriter(new File(chapter, "en.txt"));
                FileWriter fileWriter2 = new FileWriter(new File(chapter, "ru.txt"));
                fileWriter1.append(stringBuilder1);
                fileWriter2.append(stringBuilder2);
                fileWriter1.flush();
                fileWriter1.close();
                fileWriter2.flush();
                fileWriter2.close();
            }
        }
    }

    public static void update() {
        new Thread(() -> {
            try {
                File file1 = new File("Parallator.jar");
                file1.delete();
                URL website = new URL("https://github.com/KursX/Parallator/raw/master/release/Parallator.jar");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("Parallator.jar");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void csv(Scene scene, Map<String, List<String>> map, final File name) {
        try {
            File file = showDirectoryChooser(scene);
            if (file == null) return;
            StringBuilder builder = new StringBuilder();
            int position = 0;
            while (true) {
                boolean found = false;
                for (String key : map.keySet()) {
                    if (map.get(key).size() > position) {
                        String value = map.get(key).get(position);
                        builder.append(value.replace(";", "\\;")).append(";");
                        found = true;
                    } else {
                        builder.append(";");
                    }
                }
                position++;
                builder.append("\n");
                if (!found) break;
            }

            FileWriter fileWriter = new FileWriter(new File(file, StringUtils.getBaseName(name) + ".csv"));
            fileWriter.append(builder);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void html(Scene scene, MainController controller) {
        try {
            File file = showDirectoryChooser(scene);
            if (file == null) return;
            writeHtml(controller.getTextMap(), file, controller.feelChapters(controller.getFile()), controller.getConfig().enc1());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeHtml(Map<String, List<String>> map, final File path, List<Chapter> list, String encoding) throws ValidationException, IOException {

        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n<body>\n<table cellspacing = \"0\" border = \"1\" style=\"width:100%;\">");
        builder.append("\n<tr>");
        for (Chapter chapter : list) {
            if (chapter.getChapters() == null) {
                int width = 100 / map.keySet().size();
                for (String key : map.keySet()) {
                    builder.append("\n<td width=\"").append(width).append("%\">").append(key).append("</td>");
                }
                builder.append("</tr>");
                int position = 0;
                while (true) {
                    boolean found = false;
                    for (String key : map.keySet()) {
                        builder.append("\n<td width=\"").append(width).append("%\">");
                        if (map.get(key).size() > position) {
                            String value = chapter.paragraphs.get(position).get(key);
                            builder.append(value);
                            found = true;
                        }
                        builder.append("</td>");
                    }
                    position++;
                    builder.append("</tr>");
                    if (!found) break;
                }
            } else {
                writeHtml(map, new File(path, chapter.getChapterName()), chapter.getChapters(), encoding);
            }
        }

        builder.append("\n</table>\n</body>\n</html>");
        path.mkdir();
        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream(new File(path, "book.html")), encoding);
        writer1.append(builder);
        writer1.flush();
        writer1.close();
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
            e.printStackTrace();
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
}
