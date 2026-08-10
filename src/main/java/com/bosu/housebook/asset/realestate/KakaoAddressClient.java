package com.bosu.housebook.asset.realestate;

import com.bosu.housebook.asset.realestate.dto.AddressCandidateResponse;
import com.bosu.housebook.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 카카오 로컬 API의 주소 검색(주소 → 좌표/법정동코드)을 호출한다. 응답의 b_code(법정동코드,
 * 10자리) 앞 5자리가 국토교통부 실거래가 API가 요구하는 시군구 단위 LAWD_CD와 동일하다.
 */
@Component
public class KakaoAddressClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KakaoAddressClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public List<AddressCandidateResponse> searchAddresses(String apiKey, String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create("https://dapi.kakao.com/v2/local/search/address.json?query=" + encodedQuery
                + "&size=10");

        JsonNode root;
        try {
            String rawJson = restClient.get()
                    .uri(uri)
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(String.class);
            root = objectMapper.readTree(rawJson);
        } catch (Exception e) {
            throw ApiException.badRequest("주소 검색에 실패했습니다.");
        }

        List<AddressCandidateResponse> results = new ArrayList<>();
        if (root == null) {
            return results;
        }
        JsonNode documents = root.path("documents");
        if (!documents.isArray()) {
            return results;
        }
        for (JsonNode doc : documents) {
            AddressCandidateResponse candidate = toCandidate(doc);
            if (candidate != null) {
                results.add(candidate);
            }
        }
        return results;
    }

    private AddressCandidateResponse toCandidate(JsonNode doc) {
        JsonNode address = doc.path("address");
        if (address.isMissingNode() || address.isNull()) {
            return null;
        }
        String jibunAddress = text(address, "address_name");
        String bCode = text(address, "b_code");
        String dongName = text(address, "region_3depth_name");
        if (bCode == null || bCode.length() < 5) {
            return null;
        }
        String lawdCd = bCode.substring(0, 5);

        JsonNode roadAddressNode = doc.path("road_address");
        String roadAddress = null;
        String buildingName = null;
        if (!roadAddressNode.isMissingNode() && !roadAddressNode.isNull()) {
            roadAddress = text(roadAddressNode, "address_name");
            buildingName = text(roadAddressNode, "building_name");
        }

        return new AddressCandidateResponse(roadAddress, jibunAddress, lawdCd, dongName, buildingName);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText().trim();
    }
}
