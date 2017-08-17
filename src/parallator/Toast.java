package parallator;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class Toast {

    public static void makeText(Stage ownerStage, String toastMsg) {
        makeText(ownerStage, toastMsg, 2000);
    }

    public static void makeText(Stage ownerStage, String toastMsg, int time) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initOwner(ownerStage);
            toastStage.setResizable(false);
            toastStage.initStyle(StageStyle.TRANSPARENT);

            Text text = new Text(toastMsg);
            text.setFont(Font.font("Verdana", 20));
            text.setFill(Color.WHITE);

            StackPane root = new StackPane(text);
            root.setStyle("-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 25px;");
            root.setOpacity(0);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);
            toastStage.show();

            Timeline fadeInTimeline = new Timeline();
            KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(200), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
            fadeInTimeline.getKeyFrames().add(fadeInKey1);
            fadeInTimeline.setOnFinished((ae) -> new Thread(() -> {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(200), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
                fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
                fadeOutTimeline.play();
            }).start());
            fadeInTimeline.play();
        });
    }
}
