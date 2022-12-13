package edu.yu.parallel;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SymbolCache {
    private final Map<String, SymbolData> symbolList;

    public SymbolCache(SymbolReader reader) {
        this.symbolList = reader.stream()
                .filter(data -> data.nsdq100Weight() > 0 || data.snp500Weight() > 0)
                .collect(Collectors.toMap(
                        data -> data.symbol(),
                        data -> data));
    }


    public Stream<SymbolData> stream() {
        return symbolList.values().stream();
    }

    public SymbolData getSymbolData(String symbol) {
        return symbolList.get(symbol);
    }
}
