package pl.pwr.imageapp;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastNotification {

    public enum Type { SUCCESS, ERROR, WARNING }

    public static void show(Stage owner, String message, Type type) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill:white; -fx-font-size:13px;");

        String bg = switch (type) {
            case SUCCESS -> "#27ae60";
            case ERROR   -> "#c0392b";
            case WARNING -> "#e67e22";
        };

        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(12, 20, 12, 20));
        pane.setMaxWidth(420);
        pane.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:8;");

        Popup popup = new Popup();
        popup.getContent().add(pane);
        popup.setAutoHide(true);
        popup.show(owner);

        popup.setX(owner.getX() + (owner.getWidth() - pane.getWidth()) / 2);
        popup.setY(owner.getY() + owner.getHeight() - pane.getHeight() - 60);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        fadeIn.setOnFinished(e -> stay.play());
        stay.setOnFinished(e -> fadeOut.play());
        fadeIn.play();
    }
}
