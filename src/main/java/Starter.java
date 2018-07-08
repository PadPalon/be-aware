import java.net.URL;

/**
 * used to start a {@link Pinger}
 */
public class Starter {
    private Starter() {}

    public static void main(String[] args) throws InterruptedException {
        URL sound = new Starter().getClass().getClassLoader().getResource("sounds/ba_bum.wav");
        Pinger pinger = new Pinger(sound, 1);
        pinger.start();
        Thread.sleep(5000);
        pinger.stop();
    }
}
