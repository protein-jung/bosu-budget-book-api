package com.bosu.housebook.asset.realestate;

import com.bosu.housebook.asset.realestate.dto.AddressCandidateResponse;
import com.bosu.housebook.asset.realestate.dto.RealEstateRegionResponse;
import com.bosu.housebook.asset.realestate.dto.RealEstateTradeResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.config.KakaoProperties;
import com.bosu.housebook.config.MolitProperties;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RealEstateTradeService {

    private final MolitProperties molitProperties;
    private final KakaoProperties kakaoProperties;
    private final MolitApartmentTradeClient tradeClient;
    private final KakaoAddressClient addressClient;
    private final LawdCodeProvider lawdCodeProvider;

    public RealEstateTradeService(MolitProperties molitProperties, KakaoProperties kakaoProperties,
            MolitApartmentTradeClient tradeClient, KakaoAddressClient addressClient,
            LawdCodeProvider lawdCodeProvider) {
        this.molitProperties = molitProperties;
        this.kakaoProperties = kakaoProperties;
        this.tradeClient = tradeClient;
        this.addressClient = addressClient;
        this.lawdCodeProvider = lawdCodeProvider;
    }

    public List<AddressCandidateResponse> searchAddresses(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        if (kakaoProperties.apiKey() == null || kakaoProperties.apiKey().isBlank()) {
            throw ApiException.badRequest("카카오 주소 검색 API 키가 설정되지 않았어요. 서버 관리자에게 문의하세요.");
        }
        return addressClient.searchAddresses(kakaoProperties.apiKey(), query.trim());
    }

    public List<RealEstateRegionResponse> getRegions() {
        Map<String, List<LawdCode>> bySido = lawdCodeProvider.all().stream()
                .collect(Collectors.groupingBy(LawdCode::sido, LinkedHashMap::new, Collectors.toList()));
        return bySido.entrySet().stream()
                .map(entry -> new RealEstateRegionResponse(entry.getKey(),
                        entry.getValue().stream()
                                .map(c -> new RealEstateRegionResponse.SigunguOption(c.sigungu(), c.code()))
                                .toList()))
                .toList();
    }

    public List<RealEstateTradeResponse> searchTrades(String lawdCd, String dealYm, String complexName) {
        if (molitProperties.apiKey() == null || molitProperties.apiKey().isBlank()) {
            throw ApiException.badRequest("국토교통부 API 키가 설정되지 않았어요. 서버 관리자에게 문의하세요.");
        }
        List<RealEstateTradeResponse> trades = tradeClient.fetchTrades(molitProperties.apiKey(), lawdCd, dealYm);
        return filterByComplex(trades, complexName);
    }

    /**
     * 자산 시세 자동 갱신용: 최근 달부터 거슬러 올라가며 해당 단지의 거래가 있는 첫 달을 찾아
     * 그중 가장 최근 거래를 돌려준다. API 키가 없거나 조회에 실패하면(월별로 개별 실패 가능)
     * 예외를 던지지 않고 빈 값을 돌려줘서, 여러 자산을 한 번에 갱신할 때 한 건의 실패가
     * 전체를 막지 않게 한다.
     */
    public Optional<RealEstateTradeResponse> findLatestTrade(String lawdCd, String complexName) {
        if (molitProperties.apiKey() == null || molitProperties.apiKey().isBlank()) {
            return Optional.empty();
        }
        if (lawdCd == null || lawdCd.isBlank() || complexName == null || complexName.isBlank()) {
            return Optional.empty();
        }
        YearMonth cursor = YearMonth.now();
        for (int i = 0; i < 6; i++) {
            String dealYm = cursor.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
            List<RealEstateTradeResponse> trades;
            try {
                trades = filterByComplex(tradeClient.fetchTrades(molitProperties.apiKey(), lawdCd, dealYm),
                        complexName);
            } catch (ApiException e) {
                continue;
            }
            Optional<RealEstateTradeResponse> latest = trades.stream()
                    .filter(trade -> trade.dealDate() != null)
                    .max(Comparator.comparing(RealEstateTradeResponse::dealDate));
            if (latest.isPresent()) {
                return latest;
            }
        }
        return Optional.empty();
    }

    private List<RealEstateTradeResponse> filterByComplex(List<RealEstateTradeResponse> trades, String complexName) {
        if (complexName == null || complexName.isBlank()) {
            return trades;
        }
        // 카카오가 알려주는 건물명과 국토교통부 단지명은 띄어쓰기가 서로 다를 수 있어(예: "래미안
        // 개포 루체하임" vs "래미안개포루체하임") 공백을 제거하고 포함 여부를 비교한다.
        String needle = complexName.replaceAll("\\s+", "");
        return trades.stream()
                .filter(trade -> trade.aptName() != null && trade.aptName().replaceAll("\\s+", "").contains(needle))
                .toList();
    }
}
