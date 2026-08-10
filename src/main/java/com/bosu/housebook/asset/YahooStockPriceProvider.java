package com.bosu.housebook.asset;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Yahoo Finance의 비공식 차트 API로 주식 시세를 조회한다(키 불필요). 국내는 "005930.KS"/".KQ"
 * 접미사, 미국은 "AAPL" 그대로. 공식 API가 아니라 언젠가 응답 형식이 바뀌거나 막힐 수 있다 —
 * 그 경우 fetchPrice는 빈 Optional을 반환하므로 호출부에서 해당 종목만 갱신 실패로 처리된다.
 */
@Component
public class YahooStockPriceProvider implements AssetPriceProvider {

    /** 미국 주식 등 KRW가 아닌 통화를 원화로 환산할 때 쓰는 원달러 환율 티커. */
    static final String USD_KRW_SYMBOL = "KRW=X";

    private final RestClient restClient;

    public YahooStockPriceProvider() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<QuotedPrice> fetchPrice(String symbol) {
        try {
            ChartResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v8/finance/chart/{symbol}")
                            .queryParam("interval", "1d")
                            .queryParam("range", "1d")
                            .build(symbol))
                    .retrieve()
                    .body(ChartResponse.class);

            if (response == null || response.chart() == null || response.chart().result() == null
                    || response.chart().result().isEmpty()) {
                return Optional.empty();
            }
            Meta meta = response.chart().result().get(0).meta();
            if (meta == null || meta.regularMarketPrice() == null) {
                return Optional.empty();
            }
            return Optional.of(new QuotedPrice(BigDecimal.valueOf(meta.regularMarketPrice()), meta.currency()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<BigDecimal> fetchUsdKrwRate() {
        return fetchPrice(USD_KRW_SYMBOL).map(QuotedPrice::price);
    }

    /**
     * Yahoo Finance의 종목 검색 API. 영문/티커/숫자 검색어는 잘 동작하지만 한글 검색어를 넣으면
     * "Invalid Search Query" 400 에러를 반환한다(비공식 API의 알려진 한계) — 이 경우 국내
     * 종목은 KrStockProvider의 정적 목록으로 대신 찾도록 상위(StockSymbolSearchService)에서
     * 처리하고, 여기서는 실패 시 빈 리스트만 반환한다.
     */
    public List<StockQuote> searchSymbols(String query) {
        try {
            SearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/finance/search")
                            .queryParam("q", query)
                            .queryParam("quotesCount", 8)
                            .queryParam("newsCount", 0)
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
            if (response == null || response.quotes() == null) {
                return List.of();
            }
            return response.quotes();
        } catch (Exception e) {
            return List.of();
        }
    }

    private record ChartResponse(Chart chart) {
    }

    private record Chart(List<Result> result) {
    }

    private record Result(Meta meta) {
    }

    private record Meta(String currency, Double regularMarketPrice) {
    }

    private record SearchResponse(List<StockQuote> quotes) {
    }

    public record StockQuote(String symbol, String shortname, String longname, String exchDisp, String quoteType) {
    }
}
