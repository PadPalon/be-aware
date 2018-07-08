package ch.neukom.beaware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * used to start a {@link Pinger}
 */
public class Starter {
    private Starter() {}

    public static void main(String[] args) throws InterruptedException, IOException {
        Properties properties = getProperties();

        String soundPath = String.format("sounds/%s.wav", properties.getProperty("sound"));
        URL sound = Starter.class.getClassLoader().getResource(soundPath);
        Pinger pinger = new Pinger(sound, Integer.valueOf(properties.getProperty("interval")));

        pinger.start();
        Thread.sleep(5000);
        pinger.stop();
    }

    private static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(Starter.class.getClassLoader().getResourceAsStream("properties/settings.properties"));
        File file = new File("settings.properties");
        if(file.exists()) {
            properties.load(new FileInputStream(file));
        }
        return properties;
    }
}
