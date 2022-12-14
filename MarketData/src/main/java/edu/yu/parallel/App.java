package edu.yu.parallel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;


public class App {
    final static Logger logger = LogManager.getLogger(App.class);

    static {
        Configurator.setLevel("edu.yu.parallel", Level.INFO);
    }

    public static void main(String[] args) throws IOException {

        var symbolCache = new SymbolCache(new SymbolReader("nasdaq"));

        logger.info("{} symbols", symbolCache.stream().count());

        var reports = new MarketDataReports(symbolCache);

        var start = Instant.now();
        reports.generateCloseAboveMidPriceReport(new FileOutputStream("CloseAboveMid.csv"));
        var duration = Duration.between(start, Instant.now()).toMillis();
        logger.info("CloseAboveMid: duration={}", duration);

        start = Instant.now();
        reports.generateNASDAQ100CompositeReport(new FileOutputStream("NASDAQ_100.csv"));
        duration = Duration.between(start, Instant.now()).toMillis();
        logger.info("NASDAQ_100: duration={}", duration);
    }
}
