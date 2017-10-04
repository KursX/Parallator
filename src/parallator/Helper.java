package parallator;


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

    public static final String[] dividers = {
            DIV1, DIV2,
    };

    public static void importFb2(List<Section> sections1, File directory, String from) {
        try {
            write(sections1, directory, from);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(List<Section> sections1,
                             File directory, String from) throws IOException {
        for (int i = 0; i < sections1.size(); i++) {
            Section section1 = sections1.get(i);
            File book = new File(directory + "/" + (i + 1) + "# " + section1.getTitleString(". ") + "Chapter " + (i + 1));
            book.mkdirs();
            if (!section1.getSections().isEmpty()) {
                write(section1.getSections(), book, from);
            } else {
                StringBuilder stringBuilder1 = new StringBuilder();
                if (i < sections1.size()) {
                    section1.getElements().stream().filter(p -> p.getText() != null && !p.getText().isEmpty())
                            .forEach(p -> stringBuilder1.append(p.getText()).append("\n\n"));
                }
                FileWriter fileWriter1 = new FileWriter(new File(book, from + ".txt"));
                fileWriter1.append(stringBuilder1);
                fileWriter1.flush();
                fileWriter1.close();
            }
        }
    }

    public static void write(List<Chapter> sections1, String en, File directory) throws IOException {
        for (int i = 0; i < sections1.size(); i++) {
            Chapter section1 = sections1.get(i);
            File book = null;
            for (File file : directory.listFiles()) {
                if (file.getName().startsWith((i + 1) + "# ")) {
                    book = file;
                    break;
                }
            }
            if (section1.getChapters() != null) {
                write(section1.getChapters(), en, book);
            } else {
//                StringBuilder stringBuilder1 = new StringBuilder();
//                for (Paragraph paragraph : section1.paragraphs) {
//                    if (en) {
//                        stringBuilder1.append(paragraph.getEn()).append("\n\n");
//                    } else {
//                        stringBuilder1.append(paragraph.getRu()).append("\n\n");
//                    }
//                }
//                FileWriter fileWriter1 = new FileWriter(new File(book, en ? "en.txt" : "ru.txt"));
//                fileWriter1.append(stringBuilder1);
//                fileWriter1.flush();
//                fileWriter1.close();
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

    public static void html(Scene scene, Map<String, List<String>> map, final File name) {
        try {
            File file = showDirectoryChooser(scene);
            if (file == null) return;
            int width = 100 / map.keySet().size();
            StringBuilder builder = new StringBuilder();
            builder.append("<html>\n<body>\n<table cellspacing = \"0\" border = \"1\" style=\"width:100%;\">");
            builder.append("\n<tr>");
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
                        String value = map.get(key).get(position);
                        builder.append(value);
                        found = true;
                    }
                    builder.append("</td>");
                }
                position++;
                builder.append("</tr>");
                if (!found) break;
            }
            builder.append("\n</table>\n</body>\n</html>");
            FileWriter fileWriter = new FileWriter(new File(file, StringUtils.getBaseName(name) + ".html"));
            fileWriter.append(builder);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
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
