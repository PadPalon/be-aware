package ch.neukom.beaware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * used to start a {@link Pinger}
 */
public class Starter extends Application {
    Pinger pinger = null;

    @Override
    public void start(Stage stage) throws Exception {
        Properties properties = getProperties();

        Spinner<Integer> spinner = new Spinner<>(0, 60, Integer.valueOf(properties.getProperty("interval")));
        TextField soundField = new TextField(properties.getProperty("sound"));
        VBox leftSide = createLeftSide(properties, spinner, soundField);

        VBox rightSide = createRightSide(spinner, soundField);

        HBox root = new HBox(leftSide, rightSide);
        root.setPadding(new Insets(10));
        root.setSpacing(30);

        Scene scene = new Scene(root, 250, 150);

        stage.setTitle("Be Aware");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createLeftSide(Properties properties, Spinner<Integer> spinner, TextField soundField) {
        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setOnAction((ActionEvent event) -> {
            properties.setProperty("interval", spinner.getValue().toString());
            properties.setProperty("sound", soundField.getText());
            try {
                File customProperties = new File("settings.properties");
                customProperties.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(customProperties);
                properties.store(outputStream, "saved from UI");
                outputStream.close();
            } catch (Exception e) {
                System.out.println(String.format("Exception: %s", e.getMessage()));
            }
        });

        VBox box = new VBox(spinner, soundField, saveButton);
        box.setSpacing(10);
        return box;
    }

    private VBox createRightSide(Spinner<Integer> spinner, TextField soundField) {
        Button startButton = new Button();
        startButton.setText("Start");
        startButton.setOnAction((ActionEvent event) -> {
            String soundPath = String.format("sounds/%s.wav", soundField.getText());
            URL sound = Starter.class.getClassLoader().getResource(soundPath);
            pinger = new Pinger(sound, spinner.getValue());
            pinger.start();
        });

        Button stopButton = new Button();
        stopButton.setText("Stop");
        stopButton.setOnAction((ActionEvent event) -> {
            pinger.stop();
            pinger = null;
        });

        Button quitButton = new Button();
        quitButton.setText("Quit");
        quitButton.setOnAction((ActionEvent event) -> {
            if(pinger != null) {
                pinger.stop();
            }
            Platform.exit();
        });

        VBox box = new VBox(startButton, stopButton, quitButton);
        box.setSpacing(10);
        return box;
    }

    private static Properties getProperties() throws IOException {
        Properties properties = new Properties();

        InputStream resourceProperties = Starter.class.getClassLoader().getResourceAsStream("properties/settings.properties");
        properties.load(resourceProperties);
        resourceProperties.close();

        File file = new File("settings.properties");
        if(file.exists()) {
            FileInputStream stream = new FileInputStream(file);
            properties.load(stream);
            stream.close();
        }

        return properties;
    }
}
