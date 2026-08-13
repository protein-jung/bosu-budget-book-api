package com.bosu.housebook.asset.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 사용자가 붙여넣은 encar.com 차량 목록 URL(예: car.encar.com/list/car?...&search={...})에서
 * 검색 조건(search 파라미터의 JSON 중 action 표현식)을 그대로 꺼내, encar가 실제로 매물 목록을
 * 그리는 데 쓰는 비공식 JSON API(api.encar.com/search/car/list/general)를 직접 호출한다.
 * 응답의 매물별 Price는 만원 단위라 원화로 환산해서 평균을 낸다.
 */
@Component
public class EncarPriceClient {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EncarPriceClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.encar.com")
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", USER_AGENT)
                .defaultHeader("Referer", "https://car.encar.com/")
                .defaultHeader("Origin", "https://car.encar.com")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /** 매물이 하나도 없거나 URL 형식이 다르거나 호출에 실패하면 empty. */
    public Optional<BigDecimal> fetchAveragePrice(String encarListUrl) {
        String searchExpression = extractSearchExpression(encarListUrl);
        if (searchExpression == null) {
            return Optional.empty();
        }

        try {
            String rawJson = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search/car/list/general")
                            .queryParam("count", "true")
                            .queryParam("q", searchExpression)
                            .queryParam("sr", "|MobileModifiedDate|0|20")
                            .build())
                    .retrieve()
                    .body(String.class);
            // Spring Boot 4.1의 기본 컨버터는 Jackson 3라 JsonNode(Jackson 2)를 바로 못 받는다.
            // 원문 문자열로 받아 Jackson 2 ObjectMapper로 직접 파싱한다.
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode results = root.path("SearchResults");
            if (!results.isArray() || results.isEmpty()) {
                return Optional.empty();
            }

            BigDecimal sumManwon = BigDecimal.ZERO;
            int count = 0;
            for (JsonNode item : results) {
                JsonNode priceNode = item.path("Price");
                if (priceNode.isMissingNode() || priceNode.isNull()) {
                    continue;
                }
                sumManwon = sumManwon.add(BigDecimal.valueOf(priceNode.asDouble()));
                count++;
            }
            if (count == 0) {
                return Optional.empty();
            }

            BigDecimal averageManwon = sumManwon.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            BigDecimal averageWon = averageManwon.multiply(BigDecimal.valueOf(10_000))
                    .setScale(0, RoundingMode.HALF_UP);
            return Optional.of(averageWon);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String extractSearchExpression(String encarListUrl) {
        if (encarListUrl == null || encarListUrl.isBlank()) {
            return null;
        }
        try {
            String rawQuery = URI.create(encarListUrl.trim()).getRawQuery();
            if (rawQuery == null) {
                return null;
            }
            String searchParam = null;
            for (String pair : rawQuery.split("&")) {
                int idx = pair.indexOf('=');
                if (idx < 0 || !"search".equals(pair.substring(0, idx))) {
                    continue;
                }
                searchParam = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                break;
            }
            if (searchParam == null) {
                return null;
            }
            JsonNode action = objectMapper.readTree(searchParam).path("action");
            return action.isMissingNode() || action.isNull() ? null : action.asText();
        } catch (Exception e) {
            return null;
        }
    }
}
