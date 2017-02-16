package net.yangziwen.hqlformatter.util;

import java.util.concurrent.TimeUnit;

public class SleepUtil {

    public static void sleepSecondsQuietly(long time) {
        sleepQuietly(time, TimeUnit.SECONDS);
    }

    public static void sleepQuietly(long time, TimeUnit unit) {
        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
