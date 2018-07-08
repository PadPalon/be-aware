package ch.neukom.beaware;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * plays a sound in a loop with a predefined interval
 */
public class Pinger {
    private final URL clipSource;
    private final int interval;

    private Timer timer = null;
    private Clip clip = null;

    public Pinger(URL clipSource,
                  int interval) {
        this.clipSource = clipSource;
        this.interval = interval;
    }

    public void start() {
        TimerTask task = getPingTask();
        if(task != null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 0, interval * 1000L);
        }
    }

    @Nullable
    private TimerTask getPingTask() {
        try {
            clip = AudioSystem.getClip();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(clipSource);
            clip.open(audioStream);
            audioStream.close();

            return new TimerTask() {
                @Override
                public void run() {
                    clip.setFramePosition(0);
                    clip.start();
                    while(clip.getFrameLength() > clip.getFramePosition()) {
                        //wait for playback to finish
                    }
                }
            };
        } catch (Exception e) {
            System.out.println(String.format("Exception: %s", e.getMessage()));
        }

        return null;
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
