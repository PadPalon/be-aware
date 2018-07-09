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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * used to start a {@link Pinger}
 */
public class ConfigurationUi extends Application {
    @Nullable
    private Pinger pinger = null;
    @Nullable
    private Stage stage = null;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Properties properties = getProperties();

        Label intervalLabel = new Label("Interval in seconds:");
        Spinner<Integer> intervalSpinner = new Spinner<>(0, 60, Integer.valueOf(properties.getProperty("interval")));

        Label soundLabel = new Label("Sound:");
        ComboBox<SoundConfig> soundSelect = createSoundSelect(properties);

        VBox controls = createControls(intervalSpinner, soundSelect, properties);
        TitledPane configuration = createConfigurationPanel(properties, intervalLabel, intervalSpinner, soundLabel, soundSelect);
        VBox bottomBar = createBottomBar();

        BorderPane borderPane = createContentRoot(configuration, controls, bottomBar);

        Scene scene = new Scene(borderPane, 500, 160);

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
        Properties soundConfigs = loadProperties("properties/sounds.properties");

        return soundConfigs.stringPropertyNames()
            .stream()
            .map(name -> new SoundConfig(name, soundConfigs.getProperty(name)))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private TitledPane createConfigurationPanel(Properties properties,
                                                Label intervalLabel,
                                                Spinner<Integer> intervalSpinner,
                                                Label soundLabel,
                                                ComboBox<SoundConfig> soundSelect) {
        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setOnAction((ActionEvent event1) -> {
            properties.setProperty("interval", intervalSpinner.getValue().toString());
            properties.setProperty("sound", soundSelect.getValue().getFileName());
            updateProperties(properties);
        });

        GridPane settings = new GridPane();
        settings.setVgap(10);
        settings.setHgap(10);
        settings.add(intervalLabel, 0, 0);
        settings.add(intervalSpinner, 1, 0);
        settings.add(soundLabel, 0, 1);
        settings.add(soundSelect, 1, 1);
        settings.add(saveButton, 1, 2);
        TitledPane configuration = new TitledPane("Configuration", settings);
        configuration.setExpanded(false);
        configuration.expandedProperty().addListener(event -> {
            if(configuration.isExpanded()) {
                stage.setHeight(320);
            } else {
                stage.setHeight(200);
            }
        });
        return configuration;
    }

    private VBox createControls(Spinner<Integer> intervalSpinner,
                                ComboBox<SoundConfig> soundSelect,
                                Properties properties) {
        String volume = properties.getProperty("volume");

        Label volumeLabel = new Label(String.format("Volume: %s", volume));
        ScrollBar volumeBar = createVolumeBar(volumeLabel, Integer.valueOf(volume), properties);

        Button startButton = new Button();
        startButton.setText("Start");

        Button stopButton = new Button();
        stopButton.setDisable(true);
        stopButton.setText("Stop");

        startButton.setOnAction((ActionEvent event) -> {
            intervalSpinner.setDisable(true);
            soundSelect.setDisable(true);
            startButton.setDisable(true);
            pinger = new Pinger(soundSelect.getValue().getFileName(), intervalSpinner.getValue());
            pinger.start(((float) volumeBar.getValue()) / 100);
            stopButton.setDisable(false);
        });
        stopButton.setOnAction((ActionEvent event) -> {
            intervalSpinner.setDisable(false);
            soundSelect.setDisable(false);
            stopButton.setDisable(true);
            if(pinger != null) {
                pinger.stop();
            }
            pinger = null;
            startButton.setDisable(false);
        });

        HBox buttonBox = new HBox(startButton, stopButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        VBox controls = new VBox(volumeLabel, volumeBar, buttonBox);
        controls.setSpacing(10);
        return controls;
    }

    private ScrollBar createVolumeBar(Label volumeLabel, Integer initialValue, Properties properties) {
        ScrollBar volumeBar = new ScrollBar();
        volumeBar.setMin(0);
        volumeBar.setMax(100);
        volumeBar.setValue(initialValue);
        volumeBar.valueProperty().addListener(event -> {
            double value = volumeBar.getValue();
            int roundedValue = (int) value;
            String volumeString = String.format("Volume: %s", roundedValue);

            volumeLabel.setText(volumeString);

            if(pinger != null) {
                pinger.updateLevel(((float) value) / 100);
            }

            properties.setProperty("volume", Integer.toString(roundedValue));
            updateProperties(properties);
        });
        return volumeBar;
    }

    private VBox createBottomBar() {
        Button quitButton = new Button();
        quitButton.setText("Quit");
        quitButton.setOnAction((ActionEvent event) -> {
            if(pinger != null) {
                pinger.stop();
            }
            Platform.exit();
        });

        VBox bottomBar = new VBox(quitButton);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        return bottomBar;
    }

    private BorderPane createContentRoot(Node center, Node top, Node bottom) {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(top);
        BorderPane.setMargin(center, new Insets(10, 0, 10, 0));
        BorderPane.setAlignment(center, Pos.TOP_CENTER);
        borderPane.setCenter(center);
        borderPane.setBottom(bottom);
        borderPane.setPadding(new Insets(10));
        return borderPane;
    }

    private static Properties getProperties() throws IOException {
        Properties properties = loadProperties("properties/settings.properties");

        File file = new File("settings.properties");
        if(file.exists()) {
            FileInputStream stream = new FileInputStream(file);
            properties.load(stream);
            stream.close();
        }

        return properties;
    }

    private static Properties loadProperties(String propertiesFile) throws IOException {
        Properties properties = new Properties();

        InputStream resourceProperties = ConfigurationUi.class.getClassLoader().getResourceAsStream(propertiesFile);
        properties.load(resourceProperties);
        resourceProperties.close();

        return properties;
    }

    private void updateProperties(Properties properties) {
        try {
            File customProperties = new File("settings.properties");
            customProperties.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(customProperties);
            properties.store(outputStream, "saved from UI");
            outputStream.close();
        } catch (Exception e) {
            System.out.println(String.format("Exception: %s", e.getMessage()));
        }
    }
}
