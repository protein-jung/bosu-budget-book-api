package com.bosu.housebook.asset;

import com.bosu.housebook.asset.dto.StockSymbolCandidate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StockSymbolSearchService {

    private static final int MAX_RESULTS = 10;
    private static final Set<String> ALLOWED_QUOTE_TYPES = Set.of("EQUITY", "ETF");

    private final KrStockProvider krStockProvider;
    private final YahooStockPriceProvider yahooStockPriceProvider;

    public StockSymbolSearchService(KrStockProvider krStockProvider, YahooStockPriceProvider yahooStockPriceProvider) {
        this.krStockProvider = krStockProvider;
        this.yahooStockPriceProvider = yahooStockPriceProvider;
    }

    public List<StockSymbolCandidate> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String needle = query.trim();

        List<StockSymbolCandidate> results = new ArrayList<>();
        Set<String> seenSymbols = new LinkedHashSet<>();

        for (KrStock stock : krStockProvider.all()) {
            if (results.size() >= MAX_RESULTS) {
                break;
            }
            if (stock.name().contains(needle) || stock.code().equals(needle)) {
                String symbol = stock.yahooSymbol();
                if (seenSymbols.add(symbol)) {
                    results.add(new StockSymbolCandidate(symbol, stock.name(), stock.market()));
                }
            }
        }

        // Yahoo 검색은 한글 검색어를 거부하므로(비공식 API 한계) 실패해도 위 국내 결과는 그대로 둔다.
        if (results.size() < MAX_RESULTS) {
            for (YahooStockPriceProvider.StockQuote quote : yahooStockPriceProvider.searchSymbols(needle)) {
                if (results.size() >= MAX_RESULTS) {
                    break;
                }
                if (!ALLOWED_QUOTE_TYPES.contains(quote.quoteType())) {
                    continue;
                }
                if (seenSymbols.add(quote.symbol())) {
                    String name = quote.longname() != null ? quote.longname() : quote.shortname();
                    results.add(new StockSymbolCandidate(quote.symbol(), name, quote.exchDisp()));
                }
            }
        }

        return results;
    }
}
