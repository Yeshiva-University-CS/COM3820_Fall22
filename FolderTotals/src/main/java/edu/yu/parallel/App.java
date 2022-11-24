package edu.yu.parallel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class App {
    private final static Logger logger = LogManager.getLogger(MyFolderService.class);

    static {
        Configurator.setLevel("edu.yu.parallel", Level.INFO);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var rootFolder = System.getProperty("folder", "C:\\Windows");
        var service = new MyFolderService(rootFolder);

        logger.info("Calling getPropertiesSequential()");
        var start = Instant.now();
        var properties = service.getPropertyValuesSequential();
        logResult(properties, start, Instant.now());

        logger.info("Calling getPropertiesParallel()");
        start = Instant.now();
        properties = service.getPropertyValuesParallel().get();
        logResult(properties, start, Instant.now());

        logger.info("Calling getPropertiesParallel() and then cancelling w/o stopping threads");
        var future = service.getPropertyValuesParallel();
        Thread.sleep(100); // Give some time to queue tasks
        var cancelled = future.cancel(false);
        assert (cancelled == future.isCancelled());
        logger.info("Task future is cancelled = {}\n", future.isCancelled());
        Thread.sleep(1000); // Sleep to verify if threads stopped or not

        logger.info("Calling getPropertiesParallel() and then cancelling");
        future = service.getPropertyValuesParallel();
        Thread.sleep(100); // Give some time to queue tasks
        cancelled = future.cancel(true);
        assert (cancelled == future.isCancelled());
        logger.info("Task future is cancelled = {}\n", future.isCancelled());
        Thread.sleep(1000); // Sleep to verify if threads stopped or not
    }

    private static void logResult(PropertyValues values, Instant start, Instant finish) {
        logger.info("Elapsed = {}, files={}, bytes={}, folder={}\n",
                Duration.between(start, finish).toMillis(),
                values.getFileCount(), values.getByteCount(), values.getFolderCount());
    }
}
