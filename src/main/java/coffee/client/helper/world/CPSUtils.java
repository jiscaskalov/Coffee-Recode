package coffee.client.helper.world;

import baritone.api.event.events.TickEvent;
import coffee.client.CoffeeMain;
import coffee.client.helper.event.EventSystem;
import me.x150.jmessenger.MessageSubscription;

public class CPSUtils {
    private static int clicks;
    private static int cps;
    private static int secondsClicking;
    private static long lastTime;

    private CPSUtils() {
    }

    public static void init() {
        EventSystem.manager.registerSubscribers(CPSUtils.class);
    }

    @MessageSubscription
    private static void onTick(TickEvent event) {
        long currentTime = System.currentTimeMillis();
        // Run every second
        if (currentTime - CPSUtils.lastTime >= 1000) {
            if (CPSUtils.cps == 0) {
                CPSUtils.clicks = 0;
                CPSUtils.secondsClicking = 0;
            } else {
                CPSUtils.lastTime = currentTime;
                CPSUtils.secondsClicking++;
                CPSUtils.cps = 0;
            }
        }
    }


    public static void onAttack() {
        CPSUtils.clicks++;
        CPSUtils.cps++;
    }

    public static int getCpsAverage() {
        return clicks / (secondsClicking == 0 ? 1 : secondsClicking);
    }
}
