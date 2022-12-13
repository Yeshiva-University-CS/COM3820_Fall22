package edu.yu.parallel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SymbolReader {
    private final String fileName;
    private final Function<String, List<String>> csvLineParser = line -> Arrays.asList(line.split(","));
    private final Function<List<String>, SymbolData> inputToRecord = input -> {

        var symbol = input.get(0);
        var company = input.get(1);
        var etf = Boolean.valueOf(input.get(2));
        var nweight = Double.valueOf(input.get(3));
        var sweight = Double.valueOf(input.get(4));

        return new SymbolData(symbol, company, etf, nweight, sweight);
    };

    public SymbolReader(String symbolFile) {
        this.fileName = "/" + symbolFile + ".symbols.csv";
    }

    public Stream<SymbolData> stream() {
        return fromFile().skip(1).map(csvLineParser).map(inputToRecord);
    }

    private Stream<String> fromFile() {
        InputStream csvFileStream = getClass().getResourceAsStream(this.fileName);
        return new BufferedReader(new InputStreamReader(csvFileStream)).lines();
    }


}
