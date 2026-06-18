package pl.pwr.imageapp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    private Stage primaryStage;

    private Image originalImage;
    private Image processedImage;
    private boolean operationPerformed = false;

    private ImageView originalView;
    private ImageView processedView;
    private ComboBox<String> operationsCombo;
    private Button executeButton;
    private Button loadButton;
    private Button saveButton;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Edytor obrazow - Politechnika Wrocławska");

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildFooter());

        Scene scene = new Scene(root, 900, 680);
        URL css = getClass().getResource("styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private HBox buildHeader() {
        ImageView logo = new ImageView();
        InputStream in = getClass().getResourceAsStream("/imageapp/logo_pwr.png");
        if (in != null) {
            logo.setImage(new Image(in));
            logo.setFitHeight(48);
            logo.setPreserveRatio(true);
        }
        Label title = new Label("Edytor obrazow");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:white;");

        HBox header = new HBox(15, logo, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color:#0a3d62;");
        return header;
    }

    private HBox buildFooter() {
        Label author = new Label("Hubert Lozinski 280051");
        author.setStyle("-fx-font-size:12px;");
        HBox footer = new HBox(author);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8));
        footer.setStyle("-fx-background-color:#dfe6e9;");
        return footer;
    }

    private VBox buildCenter() {
        Label welcome = new Label(
                "Witaj w aplikacji do obróbki obrazów!\n" +
                "Wczytaj obraz w formacie .jpg, wybierz operację z listy i kliknij \"Wykonaj\".");
        welcome.setStyle("-fx-font-size:14px;");
        welcome.setWrapText(true);

        operationsCombo = new ComboBox<>();
        operationsCombo.setPromptText("Wybierz operacje");
        operationsCombo.setValue(null);
        operationsCombo.setPrefWidth(200);

        executeButton = new Button("Wykonaj");
        executeButton.setDisable(true);
        executeButton.setOnAction(e -> onExecute());

        loadButton = new Button("Wczytaj obraz");
        loadButton.setOnAction(e -> onLoad());

        saveButton = new Button("Zapisz obraz");
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> onSaveDialog());

        HBox fileBox = new HBox(10, loadButton, saveButton);
        HBox opBox = new HBox(10, operationsCombo, executeButton);
        opBox.setAlignment(Pos.CENTER_LEFT);

        originalView = createImageView();
        processedView = createImageView();

        VBox origBox = new VBox(5, new Label("Przed"), wrap(originalView));
        VBox procBox = new VBox(5, new Label("Po"), wrap(processedView));
        origBox.setAlignment(Pos.CENTER);
        procBox.setAlignment(Pos.CENTER);

        HBox previews = new HBox(20, origBox, procBox);
        previews.setAlignment(Pos.CENTER);

        VBox center = new VBox(15, welcome, fileBox, opBox, previews);
        center.setPadding(new Insets(20));
        return center;
    }

    private ImageView createImageView() {
        ImageView v = new ImageView();
        v.setFitWidth(380);
        v.setFitHeight(360);
        v.setPreserveRatio(true);
        return v;
    }

    private StackPane wrap(ImageView v) {
        StackPane p = new StackPane(v);
        p.setPrefSize(390, 370);
        p.setStyle("-fx-border-color:#b2bec3; -fx-border-width:1; -fx-background-color:#f5f6fa;");
        return p;
    }

    private void onLoad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Wybierz obraz");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JPG (*.jpg)", "*.jpg"));

        File file = fc.showOpenDialog(primaryStage);
        if (file == null) return;

        if (!file.getName().toLowerCase().endsWith(".jpg")) {
            ToastNotification.show(primaryStage, "ERROR: Niedozwolony format pliku", ToastNotification.Type.ERROR);
            return;
        }

        try {
            Image img = new Image(file.toURI().toString());
            if (img.isError()) {
                ToastNotification.show(primaryStage, "Nie udalo sie zaladować pliku", ToastNotification.Type.ERROR);
                return;
            }

            clearCache();

            originalImage = img;
            processedImage = img;
            operationPerformed = false;

            originalView.setImage(originalImage);
            processedView.setImage(processedImage);

            saveButton.setDisable(false);
            executeButton.setDisable(false);

            ToastNotification.show(primaryStage, "Pomyslnie zaladowano plik", ToastNotification.Type.SUCCESS);
        } catch (Exception ex) {
            ToastNotification.show(primaryStage, "Nie udało się zaladować pliku", ToastNotification.Type.ERROR);
        }
    }

    private void clearCache() {
        originalImage = null;
        processedImage = null;
        operationPerformed = false;
        originalView.setImage(null);
        processedView.setImage(null);
    }

    private void onExecute() {
        String op = operationsCombo.getValue();
        if (op == null) {
            ToastNotification.show(primaryStage, "Nie wybrano operacji do wykonania", ToastNotification.Type.WARNING);
            return;
        }
    }

    private void onSaveDialog() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Zapis obrazu");

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        if (!operationPerformed) {
            Label warn = new Label("Na pliku nie zostały wykonane żadne operacje!");
            warn.setStyle("-fx-text-fill:#e67e22; -fx-font-weight:bold;");
            warn.setWrapText(true);
            content.getChildren().add(warn);
        }

        Label header = new Label("Podaj nazwe pliku:");
        TextField nameField = new TextField();
        nameField.setPromptText("nazwa pliku");
        nameField.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().length() <= 100 ? c : null));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill:red; -fx-font-size:11px;");
        errorLabel.setVisible(false);

        Button saveBtn = new Button("Zapisz");
        Button cancelBtn = new Button("Anuluj");
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(header, nameField, errorLabel, buttons);

        cancelBtn.setOnAction(e -> {
            nameField.clear();
            errorLabel.setVisible(false);
            dialog.close();
        });

        saveBtn.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.length() < 3) {
                errorLabel.setText("Wpisz co najmniej 3 znaki");
                errorLabel.setVisible(true);
                return;
            }
            errorLabel.setVisible(false);
            saveImage(name, dialog);
        });

        dialog.setScene(new Scene(content, 380, 230));
        dialog.showAndWait();
    }

    private void saveImage(String baseName, Stage dialog) {
        String fileName = baseName + ".jpg";
        File dir = new File(System.getProperty("user.home"), "Pictures");
        if (!dir.exists()) dir.mkdirs();
        File outFile = new File(dir, fileName);

        if (outFile.exists()) {
            ToastNotification.show(primaryStage,
                    "Plik juz istnieje.",
                    ToastNotification.Type.ERROR);
            return;
        }

        try {
            BufferedImage fxImg = SwingFXUtils.fromFXImage(processedImage, null);
            BufferedImage rgb = new BufferedImage(fxImg.getWidth(), fxImg.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.getGraphics().drawImage(fxImg, 0, 0, Color.WHITE, null);
            ImageIO.write(rgb, "jpg", outFile);

            ToastNotification.show(primaryStage, "Zapisano obraz w pliku " + fileName, ToastNotification.Type.SUCCESS);
            dialog.close();
        } catch (Exception ex) {
            ToastNotification.show(primaryStage, "Nie udało się zapisać pliku " + fileName, ToastNotification.Type.ERROR);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
