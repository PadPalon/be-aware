package ch.neukom.beaware;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import static javax.sound.sampled.AudioSystem.*;
import static javax.sound.sampled.FloatControl.Type.*;

/**
 * plays a sound in a loop with a predefined interval
 */
public class Pinger {
    private final String clipSource;
    private final int interval;

    @Nullable
    private Timer timer = null;
    @Nullable
    private Clip clip = null;

    public Pinger(String clipSource,
                  int interval) {
        this.clipSource = clipSource;
        this.interval = interval;
    }

    public void start(float initialLevel) {
        TimerTask task = getPingTask(initialLevel);
        if(task != null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 0, interval * 1000L);
        }
    }

    @Nullable
    private TimerTask getPingTask(float initialLevel) {
        try {
            clip = getClip();
            loadSound();
            updateLevel(initialLevel);

            return new TimerTask() {
                @Override
                public void run() {
                    clip.setFramePosition(0);
                    clip.start();
                }
            };
        } catch (Exception e) {
            System.out.println(String.format("Exception: %s", e.getMessage()));
        }

        return null;
    }

    private void loadSound() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        String soundPath = String.format("sounds/%s", clipSource);
        try(InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(soundPath);
            BufferedInputStream bufferedStream = new BufferedInputStream(resourceStream);
            AudioInputStream audioStream = getAudioInputStream(bufferedStream)) {
            clip.open(audioStream);
        }
    }

    public void updateLevel(float level) {
        if(clip != null && clip.isControlSupported(MASTER_GAIN)) {
            FloatControl control = (FloatControl) clip.getControl(MASTER_GAIN);
            control.setValue(20f * (float) Math.log10(level));
        }
    }

    public void stop() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(clip != null) {
            clip.close();
            clip = null;
        }
    }
}
