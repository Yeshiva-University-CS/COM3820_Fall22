package edu.yu.parallel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamDelay {

    public static  <T> T delay(T item) {
        return delay(item, 1, TimeUnit.NANOSECONDS, 2);
    }
    public static  <T> T delay(T item, long delay, TimeUnit unit, int everyNthRecord) {
        if ((item.hashCode() % everyNthRecord) == 0)
            return item;

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            return executor.schedule(() -> {
                return item;
            }, delay, unit).get();
        } catch (InterruptedException e) {
            return item;
        } catch (ExecutionException e) {
            return item;
        } finally {
            executor.shutdown();
        }
    }
}
