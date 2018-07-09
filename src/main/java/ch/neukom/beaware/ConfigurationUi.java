package ch.neukom.beaware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * used to start a {@link Pinger}
 */
public class ConfigurationUi extends Application {
    @Nullable
    Pinger pinger = null;

    @Override
    public void start(Stage stage) throws Exception {
        Properties properties = getProperties();

        Spinner<Integer> spinner = new Spinner<>(0, 60, Integer.valueOf(properties.getProperty("interval")));
        ComboBox<SoundConfig> soundSelect = createSoundSelect(properties);
        VBox leftSide = createLeftSide(properties, spinner, soundSelect);

        VBox rightSide = createRightSide(spinner, soundSelect);

        HBox root = new HBox(leftSide, rightSide);
        root.setPadding(new Insets(10));
        root.setSpacing(30);

        Scene scene = new Scene(root, 250, 150);

        stage.setTitle("Be Aware");
        stage.setScene(scene);
        stage.show();
    }

    private ComboBox<SoundConfig> createSoundSelect(Properties properties) throws IOException {
        ObservableList<SoundConfig> soundOptions = loadSoundOptions();
        ComboBox<SoundConfig> soundSelect = new ComboBox<>(soundOptions);
        soundSelect.setConverter(new StringConverter<SoundConfig>() {
            @Override
            public String toString(SoundConfig soundConfig) {
                return soundConfig.getDisplay();
            }

            @Override
            public SoundConfig fromString(String name) {
                return findSoundOption(name, soundOptions, SoundConfig::getDisplay);
            }
        });
        soundSelect.setValue(findSoundOption(properties.getProperty("sound"), soundOptions, SoundConfig::getFileName));
        return soundSelect;
    }

    private SoundConfig findSoundOption(String name,
                                        List<SoundConfig> soundOptions,
                                        Function<SoundConfig, String> loadFileToCompare) {
        return soundOptions.stream()
            .filter(option -> loadFileToCompare.apply(option).equals(name))
            .findAny()
            .orElse(soundOptions.get(0));
    }

    private ObservableList<SoundConfig> loadSoundOptions() throws IOException {
        Properties soundConfigs = new Properties();

        InputStream soundProperties = ConfigurationUi.class.getClassLoader().getResourceAsStream("properties/sounds.properties");
        soundConfigs.load(soundProperties);
        soundProperties.close();

        return soundConfigs.stringPropertyNames()
            .stream()
            .map(name -> new SoundConfig(name, soundConfigs.getProperty(name)))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));


    }

    private VBox createLeftSide(Properties properties,
                                Spinner<Integer> spinner,
                                ComboBox<SoundConfig> soundSelect) {
        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setOnAction((ActionEvent event) -> {
            properties.setProperty("interval", spinner.getValue().toString());
            properties.setProperty("sound", soundSelect.getValue().getFileName());
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

        VBox box = new VBox(spinner, soundSelect, saveButton);
        box.setSpacing(10);
        return box;
    }

    private VBox createRightSide(Spinner<Integer> spinner, ComboBox<SoundConfig> soundSelect) {
        Button startButton = new Button();
        startButton.setText("Start");

        Button stopButton = new Button();
        stopButton.setDisable(true);
        stopButton.setText("Stop");

        startButton.setOnAction((ActionEvent event) -> {
            startButton.setDisable(true);
            pinger = new Pinger(soundSelect.getValue().getFileName(), spinner.getValue());
            pinger.start();
            stopButton.setDisable(false);
        });
        stopButton.setOnAction((ActionEvent event) -> {
            stopButton.setDisable(true);
            if(pinger != null) {
                pinger.stop();
            }
            pinger = null;
            startButton.setDisable(false);
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

        InputStream resourceProperties = ConfigurationUi.class.getClassLoader().getResourceAsStream("properties/settings.properties");
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
