package com.bosu.housebook.asset.realestate;

import com.bosu.housebook.asset.realestate.dto.RealEstateTradeResponse;
import com.bosu.housebook.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 국토교통부 "아파트 매매 실거래 상세자료" 공공데이터 API 호출. 결과가 1건일 때 item이 배열이
 * 아니라 객체 하나로 오는 경우가 있어(공공데이터 XML→JSON 변환 특성), JsonNode로 받아
 * 배열/객체 둘 다 방어적으로 처리한다.
 */
@Component
public class MolitApartmentTradeClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MolitApartmentTradeClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl("https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev")
                .requestFactory(requestFactory)
                .build();
    }

    public List<RealEstateTradeResponse> fetchTrades(String apiKey, String lawdCd, String dealYm) {
        // serviceKey는 base64 문자를 포함해 '+' 등 URI상 허용되지만 form-urlencoded 규약에서는
        // 공백으로 해석되는 문자를 담고 있다. UriComponentsBuilder의 queryParam()은 '+'를
        // 그대로 두어 국토교통부 서버가 공백으로 오해하고 키가 깨지므로, URLEncoder로 직접
        // 인코딩한 뒤 완성된 URI를 그대로 사용해 재인코딩(및 이로 인한 왜곡)을 막는다.
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        URI uri = URI.create("https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev"
                + "?serviceKey=" + encodedKey
                + "&LAWD_CD=" + lawdCd
                + "&DEAL_YMD=" + dealYm
                + "&numOfRows=300&pageNo=1&_type=json");

        JsonNode root;
        try {
            // Spring Boot 4.1의 기본 JSON 컨버터는 Jackson 3(tools.jackson) 기반이라
            // 클래식 Jackson 2의 JsonNode를 직접 역직렬화하지 못한다. 원문 문자열로 받아
            // 별도로 들고 있는 Jackson 2 ObjectMapper로 직접 파싱한다.
            String rawJson = restClient.get().uri(uri).retrieve().body(String.class);
            root = objectMapper.readTree(rawJson);
        } catch (Exception e) {
            throw ApiException.badRequest("국토교통부 실거래가 조회에 실패했습니다.");
        }

        List<RealEstateTradeResponse> trades = new ArrayList<>();
        if (root == null) {
            return trades;
        }

        String resultCode = root.path("response").path("header").path("resultCode").asText("");
        if (!resultCode.isEmpty() && !"000".equals(resultCode)) {
            String message = root.path("response").path("header").path("resultMsg").asText("조회에 실패했습니다.");
            throw ApiException.badRequest("국토교통부 API 오류: " + message);
        }

        JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
        if (itemsNode.isMissingNode() || itemsNode.isNull()) {
            return trades;
        }
        if (itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                trades.add(toTrade(item));
            }
        } else if (itemsNode.isObject()) {
            trades.add(toTrade(itemsNode));
        }
        return trades;
    }

    private RealEstateTradeResponse toTrade(JsonNode item) {
        String aptName = text(item, "aptNm");
        String dong = text(item, "umdNm");
        String buildingDong = text(item, "aptDong");
        String jibun = text(item, "jibun");
        BigDecimal exclusiveArea = decimal(item, "excluUseAr");
        Integer floor = integer(item, "floor");
        Integer year = integer(item, "dealYear");
        Integer month = integer(item, "dealMonth");
        Integer day = integer(item, "dealDay");
        LocalDate dealDate = year == null ? null
                : LocalDate.of(year, month == null ? 1 : month, day == null ? 1 : day);
        long dealAmount = amountInWon(text(item, "dealAmount"));
        return new RealEstateTradeResponse(aptName, dong, buildingDong, jibun, exclusiveArea, floor, dealDate,
                dealAmount);
    }

    private long amountInWon(String raw) {
        if (raw == null) {
            return 0;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Long.parseLong(digits) * 10_000L;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText().trim();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer integer(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
