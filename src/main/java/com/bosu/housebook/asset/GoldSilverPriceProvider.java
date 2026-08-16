package com.bosu.housebook.asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 금/은 시세를 Yahoo Finance의 국제 선물 시세(GC=F/SI=F, 트로이온스당 USD)로 조회해 원화/그램
 * 단가로 환산한다. 국내 금은방의 실물 매매가(부가세·세공비 프리미엄 포함)와는 차이가 있을 수
 * 있다 — 국제 시세 기준의 참고값이다.
 */
@Component
public class GoldSilverPriceProvider {

    /** 1트로이온스 = 31.1034768그램. */
    private static final BigDecimal GRAMS_PER_TROY_OUNCE = BigDecimal.valueOf(31.1034768);
    private static final Map<AssetType, String> YAHOO_SYMBOL = Map.of(
            AssetType.GOLD, "GC=F",
            AssetType.SILVER, "SI=F");

    private final YahooStockPriceProvider stockPriceProvider;

    public GoldSilverPriceProvider(YahooStockPriceProvider stockPriceProvider) {
        this.stockPriceProvider = stockPriceProvider;
    }

    public Optional<BigDecimal> fetchPricePerGramKrw(AssetType type) {
        String symbol = YAHOO_SYMBOL.get(type);
        if (symbol == null) {
            return Optional.empty();
        }
        Optional<QuotedPrice> quote = stockPriceProvider.fetchPrice(symbol);
        if (quote.isEmpty() || !"USD".equalsIgnoreCase(quote.get().currency())) {
            return Optional.empty();
        }
        return stockPriceProvider.fetchUsdKrwRate()
                .map(usdKrwRate -> quote.get().price().multiply(usdKrwRate)
                        .divide(GRAMS_PER_TROY_OUNCE, 2, RoundingMode.HALF_UP));
    }
}
