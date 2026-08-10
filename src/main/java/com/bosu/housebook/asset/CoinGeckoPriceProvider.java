package com.bosu.housebook.asset;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * CoinGecko 공개 API로 코인 시세를 조회한다(키 불필요). 흔한 티커는 CoinGecko id로 미리
 * 매핑해두고, 매핑에 없으면 입력값을 소문자로 그대로 CoinGecko id로 시도한다(고급 사용자는
 * "shiba-inu" 같은 정확한 id를 직접 입력할 수 있음).
 */
@Component
public class CoinGeckoPriceProvider implements AssetPriceProvider {

    private static final Map<String, String> SYMBOL_TO_COINGECKO_ID = Map.ofEntries(
            Map.entry("BTC", "bitcoin"),
            Map.entry("ETH", "ethereum"),
            Map.entry("XRP", "ripple"),
            Map.entry("SOL", "solana"),
            Map.entry("DOGE", "dogecoin"),
            Map.entry("ADA", "cardano"),
            Map.entry("DOT", "polkadot"),
            Map.entry("MATIC", "matic-network"),
            Map.entry("POL", "matic-network"),
            Map.entry("AVAX", "avalanche-2"),
            Map.entry("LINK", "chainlink"),
            Map.entry("TRX", "tron"),
            Map.entry("USDT", "tether"),
            Map.entry("USDC", "usd-coin"),
            Map.entry("BNB", "binancecoin"));

    private final RestClient restClient;

    public CoinGeckoPriceProvider() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.coingecko.com")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<QuotedPrice> fetchPrice(String symbol) {
        String id = SYMBOL_TO_COINGECKO_ID.getOrDefault(symbol.trim().toUpperCase(Locale.ROOT),
                symbol.trim().toLowerCase(Locale.ROOT));
        try {
            Map<String, Map<String, Double>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v3/simple/price")
                            .queryParam("ids", id)
                            .queryParam("vs_currencies", "krw")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {
                    });

            Double price = response == null ? null
                    : response.getOrDefault(id, Map.of()).get("krw");
            if (price == null) {
                return Optional.empty();
            }
            return Optional.of(new QuotedPrice(BigDecimal.valueOf(price), "KRW"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
