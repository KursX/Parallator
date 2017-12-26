package parallator.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parallator.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BookDialogController implements Initializable {

    private Stage stage;
    private Main main;
    private List<Chapter> chapters;
    private String thumbnailName = "";
    public static final ObservableList<String> langs = FXCollections.observableArrayList(
            "ru", "en", "be", "bg", "cs", "da", "de", "el", "es", "et", "fi", "fr",
            "it", "lt", "lv", "nl", "no", "pl", "pt", "sk", "sv", "tr", "tt", "uk"
    );

    public void init(Stage stage, Main main) {
        this.stage = stage;
        this.main = main;
        stage.setOnShowing(event -> {
            Config config = main.getRootController().getConfig();
            Book book = config.getBook();
            if (book != null) {
                from.getSelectionModel().select(book.getLang().split("-")[0]);
                to.getSelectionModel().select(book.getLang().split("-")[1]);
                fromField.setText(book.getName());
                toField.setText(book.getRusName());
                fromAuthor.setText(book.getAuthor());
                thumbnail.setVisible(true);
                thumbnail.setImage(new Image(new File(main.getRootController().getFile(), book.getThumbnail()).toURI().toString()));

                for (Book.Lang lang : book.getLangs()) {
                     for (LangItem field : langItems) {
                        if (field.getLang().lang.equals(lang.lang)) {
                            field.author.setText(lang.author);
                            field.name.setText(lang.name);
                            break;
                        }
                    }
                }
            }
        });
        chapters = main.getRootController().validate();
        if (chapters == null) {
            send.setDisable(true);
            Toast.makeText(stage, "Не во всех главах количество русских абзацев совпадает с количеством английских");
        }
    }

    @FXML
    ImageView thumbnail;

    @FXML
    TextField fromField, toField, fromAuthor, mail;

    @FXML
    Button send, cancel, save, thumbnailButton, addLang;

    @FXML
    ComboBox<String> from, to;

    @FXML
    VBox langsBox;

    private TextField[] fields;
    private List<LangItem> langItems = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        langsBox.setSpacing(5);
        String[] initialLangs = new String[]{
                "en", "ru", "de", "uk", "bg", "it", "es", "fr", "tr"
        };
        for (String initialLang : initialLangs) {
            LangItem langItem = new LangItem(initialLang);
            langItem.add(langsBox);
            langItems.add(langItem);
        }

        send.setOnAction(event -> new Thread(this::send).start());
        save.setOnAction(event -> {
            try {
                save();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            stage.close();
        });
        addLang.setOnAction(event -> {
            LangItem langItem = new LangItem(from.getSelectionModel().getSelectedItem());
            langItem.add(langsBox);
            langItems.add(langItem);
        });
        cancel.setOnAction(event -> stage.close());
        fields = new TextField[]{
                fromField, toField, fromAuthor
        };
        thumbnailButton.setOnAction(event -> {
            File file = Helper.showFileChooser(stage.getScene(), new FileChooser.ExtensionFilter("jpg", "*.jpg"));
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

        from.setItems(langs);
        to.setItems(langs);
        from.getSelectionModel().select("en");
        to.getSelectionModel().select("ru");
    }

    public boolean save() throws FileNotFoundException {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                Toast.makeText(stage, "Не все поля заполнены");
            }
        }
        for (LangItem field : langItems) {
            if (!field.isFilled()) {
                Toast.makeText(stage, "Не все поля заполнены");
            }
        }
        Book book = getBook();
        Config config = main.getRootController().getConfig();
        config.setBook(book);
        PrintWriter printWriter = new PrintWriter(new File(main.getRootController().getFile(), "book.json"));
        printWriter.print(new Gson().toJson(getBook(chapters)));
        printWriter.flush();
        printWriter.close();
        return true;
    }

    public void send() {
        try {
            if (!save()) return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!mail.getText().matches("^[-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@(?:[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$")) {
            Toast.makeText(stage, "Неверный email");
            return;
        }
        if (chapters != null) {
            Platform.runLater(() -> {
                send.setDisable(true);
                send.setText("Отправка");
                cancel.setDisable(true);
                cancel.setText("Ждите");
            });
            try {
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
        Book book = new Book(fromField.getText(), toField.getText(), fromAuthor.getText(),
                from.getSelectionModel().getSelectedItem(), to.getSelectionModel().getSelectedItem(), chapters);
        for (LangItem langItem : langItems) {
            book.getLangs().add(langItem.getLang());
        }
        return book;
    }

    public Book getBook() {
        Book book = new Book(fromField.getText(), toField.getText(), fromAuthor.getText(),
                from.getSelectionModel().getSelectedItem(), to.getSelectionModel().getSelectedItem(), thumbnailName);
        for (LangItem langItem : langItems) {
            book.getLangs().add(langItem.getLang());
        }
        return book;
    }
}
