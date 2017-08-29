package parallator.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parallator.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

    public void init(Stage stage, Main main) {
        this.stage = stage;
        this.main = main;
    }

    @FXML
    TextField from, to, fromField, toField, fromAuthor, toAuthor, mail;

    @FXML
    Button send, cancel;

    private TextField[] fields;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        send.setOnAction(event -> {
            Platform.runLater(() -> {
                send.setDisable(true);
                send.setText("Отправка");
                cancel.setDisable(true);
                cancel.setText("Ждите");
            });
            new Thread(this::send).start();
        });
        cancel.setOnAction(event -> stage.close());
        fields = new TextField[]{
                from, to, fromField, toField, fromAuthor, toAuthor
        };
    }

    public void send() {
        if (!mail.getText().matches("^[-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@(?:[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$")) {
            Toast.makeText(stage, "Неверный email");
            return;
        }
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                Toast.makeText(stage, "Не во всех главах количество русских абзацев совпадает с количеством английских");
                return;
            }
        }
        List<Chapter> chapters = main.getRootController().validate();
        if (chapters != null) {
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
                toAuthor.getText(), from.getText(), chapters);
    }
}
