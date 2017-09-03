package parallator.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import parallator.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DialogController implements Initializable {

    private Stage stage;
    private Main main;
    private String thumbnailName = "";

    public void init(Stage stage, Main main) {
        this.stage = stage;
        this.main = main;
        stage.setOnShowing(event -> {
            Config config = main.getRootController().getConfig();
            Book book = config.getBook();
            if (book != null) {
                from.setText(book.getLang().split("-")[0]);
                to.setText(book.getLang().split("-")[1]);
                fromField.setText(book.getName());
                toField.setText(book.getRusName());
                fromAuthor.setText(book.getEnAuthor());
                toAuthor.setText(book.getAuthor());
                thumbnail.setVisible(true);
                thumbnail.setImage(new Image(new File(main.getRootController().getFile(), book.getThumbnail()).toURI().toString()));
            }
        });

    }

    @FXML
    ImageView thumbnail;

    @FXML
    TextField from, to, fromField, toField, fromAuthor, toAuthor, mail;

    @FXML
    Button send, cancel, save, thumbnailButton;

    private TextField[] fields;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        send.setOnAction(event -> new Thread(this::send).start());
        save.setOnAction(event -> {
            save();
            stage.close();
        });
        cancel.setOnAction(event -> stage.close());
        fields = new TextField[]{
                from, to, fromField, toField, fromAuthor, toAuthor
        };
        thumbnailButton.setOnAction(event -> {
            File file = Helper.showFileChooser(stage.getScene());
            if (file != null) try {
                thumbnailName = file.getName();
                FileInputStream inStream = new FileInputStream(file);
                FileOutputStream outStream = new FileOutputStream(new File(main.getRootController().getFile(), thumbnailName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
                inStream.close();
                outStream.close();
                Image image = new Image(file.toURI().toString());
                if (image.getHeight() != 300 || image.getWidth() != 300) {
                    Toast.makeText(stage, "Загрузите картинку 300x300");
                    return;
                }
                thumbnail.setVisible(true);
                thumbnail.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean save() {
        if (thumbnailName.isEmpty()) {
            Toast.makeText(stage, "Загрузите картинку");
            return false;
        }
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                Toast.makeText(stage, "Не все поля заполнены");
                return false;
            }
        }
        Book book = getBook();
        Config config = main.getRootController().getConfig();
        config.setBook(book);
        return true;
    }

    public void send() {
        if (!save()) return;
        if (!mail.getText().matches("^[-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@(?:[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$")) {
            Toast.makeText(stage, "Неверный email");
            return;
        }
        List<Chapter> chapters = main.getRootController().validate();
        if (chapters != null) {
            Platform.runLater(() -> {
                send.setDisable(true);
                send.setText("Отправка");
                cancel.setDisable(true);
                cancel.setText("Ждите");
            });
            try {
                PrintWriter printWriter = new PrintWriter(new File(main.getRootController().getFile(), "book.json"));
                printWriter.print(new Gson().toJson(getBook(chapters)));
                printWriter.flush();
                printWriter.close();
                File zip = new File(main.getRootController().getFile().getParentFile(), "book.zip");
                if (zip.exists()) zip.delete();
                pack(main.getRootController().getFile().getAbsolutePath(), zip.getAbsolutePath());
                new MailSender(mail.getText(), "").addFile(zip).send();
                zip.delete();
                Platform.runLater(() -> stage.close());
                Toast.makeText(stage, "Отправлено. Ждите ответа от KursX. Спасибо за помощь в развитии");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(stage, "Не во всех главах количество русских абзацев совпадает с количеством английских");
        }
    }


    public static void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            zs.write(Files.readAllBytes(path));
                            zs.closeEntry();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public Book getBook(List<Chapter> chapters) {
        return new Book(fromField.getText(), toField.getText(), fromAuthor.getText(),
                toAuthor.getText(), from.getText(), toField.getText(), chapters);
    }

    public Book getBook() {
        return new Book(fromField.getText(), toField.getText(), fromAuthor.getText(),
                toAuthor.getText(), from.getText(), toField.getText(), thumbnailName);
    }
}
