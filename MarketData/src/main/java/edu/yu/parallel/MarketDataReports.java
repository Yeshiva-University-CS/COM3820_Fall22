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
     * MSFT,844
     * VRSK,843
     * ADBE,841
     * INTU,840
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
     * 2017-01-03,7624.927863,7474.788626,7547.201970,7566.424213,3475185756.440001,7156.133505
     * 2017-01-04,7657.954486,7541.022483,7578.069394,7608.906997,3034456813.144000,7198.486098
     * 2017-01-05,7690.347497,7566.646364,7606.050444,7640.882928,3152182093.596000,7230.710732

     * ...
     * 2022-11-29,19910.155576,19482.617342,19776.978908,19660.611746,2435208268.800000,19660.611746
     * 2022-11-30,20614.196690,19543.308255,19704.378422,20550.018541,3976500312.100000,20550.018541
     *
     * @param outputStream the OutputStream to save the report to
     */
    public void generateNASDAQ100CompositeReport(OutputStream outputStream) {

    }
}
