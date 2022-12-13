package edu.yu.parallel;

import java.io.OutputStream;


public class MarketDataReports {

    private final SymbolCache symbolCache;

    public MarketDataReports(SymbolCache symbolCache) {
        this.symbolCache = symbolCache;
    }


    /***
     * Generate a report for all the NASDAQ symbols for which data has been provided in
     * the resources/data directory.
     *
     * The report should be in CSV format and contain two columns:
     * 1) Symbol - (string) the symbol
     * 2) Count - (number) the number days on which the closing price for the given symbol
     *            was greater than the midpoint of the high and low for that day
     *
     *  The report should have a header row with columns names: symbol,count
     *
     *  The report should be ordered according the count in descending order. If two symbols
     *  have the same count, then they should be ordered alphabetically according to the symbol.
     *
     *  e.g.
     * Symbol,Count
     * SLQD,878
     * EPP,877
     * SPYG,877
     * WBIG,845
     * WBIL,844
     * ...
     *
     * @param outputStream the OutputStream to save the report to
     */
    public void generateCloseAboveMidPriceReport(OutputStream outputStream) {

    }

    /***
     * Generate a market data report in the same format as the market data files that you are using
     * as input.  The report should be a composite of all the symbols that have been tagged as being
     * part of the NASDAQ 100 index -- i.e. those symbols that have a participation weights greater
     * than 0.
     *
     * When aggregating your numbers, you must apply the participation weights to scale the values
     * for each symbol. Apply the weight to all price and volume fields.
     *
     * e.g.
     * Date,High,Low,Open,Close,Volume,Adj Close
     * 2017-01-03,28.8700008392334,28.170000076293945,28.600000381469727,28.829999923706055,2746400.0,28.555755615234375
     * 2017-01-04,30.299999237060547,29.020000457763672,29.020000457763672,30.260000228881836,3339500.0,29.972152709960938
     * 2017-01-05,30.93000030517578,30.114999771118164,30.18000030517578,30.649999618530273,2444600.0,30.358440399169922
     * ...
     * 2022-11-29,48.3129997253418,47.45100021362305,47.779998779296875,47.70000076293945,3632200.0,47.70000076293945
     * 2022-11-30,50.4900016784668,47.630001068115234,48.880001068115234,50.130001068115234,6778400.0,50.130001068115234
     *
     * @param outputStream the OutputStream to save the report to
     */
    public void generateNASDAQ100CompositeReport(OutputStream outputStream) {

    }
}
