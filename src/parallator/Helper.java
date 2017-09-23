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

    public static void importFb2(List<Section> sections1, List<Section> sections2, File directory) {
        try {
            write(sections1, sections2, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(List<Section> sections1, List<Section> sections2, File directory) throws IOException {
        for (int i = 0; i < sections1.size(); i++) {
            Section section1 = sections1.get(i);
            Section section2 = sections2.get(i);
            File book = new File(directory + "/" + (i + 1) + "# " + section1.getTitleString(". "));
            book.mkdirs();
            if (!section1.getSections().isEmpty()) {
                write(section1.getSections(), section2.getSections(), book);
            } else {
                StringBuilder stringBuilder1 = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                if (i < sections1.size()) {
                    section1.getParagraphs().stream().filter(p -> p.getP() != null && !p.getP().isEmpty())
                            .forEach(p -> stringBuilder1.append(p.getP()).append("\n\n"));
                }
                if (i < sections2.size()) {
                    section2.getParagraphs().stream().filter(p -> p.getP() != null && !p.getP().isEmpty())
                            .forEach(p -> stringBuilder2.append(p.getP()).append("\n\n"));
                }
                FileWriter fileWriter1 = new FileWriter(new File(book, "1.txt"));
                FileWriter fileWriter2 = new FileWriter(new File(book, "2.txt"));
                fileWriter1.append(stringBuilder1);
                fileWriter2.append(stringBuilder2);
                fileWriter1.flush();
                fileWriter2.flush();
                fileWriter1.close();
                fileWriter2.close();
            }
        }
    }

    public static void write(List<Chapter> sections1, boolean en, File directory) throws IOException {
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
                StringBuilder stringBuilder1 = new StringBuilder();
                for (Paragraph paragraph : section1.paragraphs) {
                    if (en) {
                        stringBuilder1.append(paragraph.getEn()).append("\n\n");
                    } else {
                        stringBuilder1.append(paragraph.getRu()).append("\n\n");
                    }
                }
                FileWriter fileWriter1 = new FileWriter(new File(book, en ? "1.txt" : "2.txt"));
                fileWriter1.append(stringBuilder1);
                fileWriter1.flush();
                fileWriter1.close();
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
