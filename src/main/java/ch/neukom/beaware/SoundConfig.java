package ch.neukom.beaware;

/**
 * contains the filename where to load a sound and a display name to show the user
 */
public class SoundConfig {
    private final String fileName;
    private final String display;

    public SoundConfig(String fileName, String display) {
        this.fileName = fileName;
        this.display = display;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplay() {
        return display;
    }
}
