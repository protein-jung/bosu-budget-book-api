package com.bosu.housebook.asset;

import com.bosu.housebook.asset.realestate.RealEstateTradeService;
import com.bosu.housebook.asset.realestate.dto.RealEstateTradeResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * STOCK/CRYPTO 자산의 시세, REAL_ESTATE 자산의 국토부 실거래가를 갱신한다. 하나가 실패해도(비공식
 * API 오류, 잘못된 심볼, 해당 단지 최근 거래 없음 등) 나머지 자산은 계속 갱신하고, 실패 건수만
 * 결과에 담아 돌려준다. 원/달러 환율은 새로고침 한 번에 최대 한 번만 조회해서(달러 표시 종목이
 * 여러 개여도) 재사용한다.
 */
@Service
public class AssetPriceService {

    private final YahooStockPriceProvider stockPriceProvider;
    private final CoinGeckoPriceProvider cryptoPriceProvider;
    private final RealEstateTradeService realEstateTradeService;

    public AssetPriceService(YahooStockPriceProvider stockPriceProvider, CoinGeckoPriceProvider cryptoPriceProvider,
            RealEstateTradeService realEstateTradeService) {
        this.stockPriceProvider = stockPriceProvider;
        this.cryptoPriceProvider = cryptoPriceProvider;
        this.realEstateTradeService = realEstateTradeService;
    }

    public record RefreshResult(int updatedCount, int failedCount) {
    }

    public RefreshResult refreshPrices(List<Asset> assets) {
        BigDecimal[] usdKrwRateCache = new BigDecimal[1];
        int updated = 0;
        int failed = 0;

        for (Asset asset : assets) {
            if (asset.getType() == AssetType.REAL_ESTATE) {
                if (asset.getLawdCd() == null || asset.getLawdCd().isBlank() || asset.getComplexName() == null
                        || asset.getComplexName().isBlank()) {
                    continue;
                }
                Optional<RealEstateTradeResponse> latest = realEstateTradeService
                        .findLatestTrade(asset.getLawdCd(), asset.getComplexName());
                if (latest.isEmpty()) {
                    failed++;
                    continue;
                }
                asset.updatePrice(BigDecimal.valueOf(latest.get().dealAmount()), LocalDateTime.now());
                updated++;
                continue;
            }

            if (!asset.getType().isLivePriced() || asset.getSymbol() == null || asset.getSymbol().isBlank()) {
                continue;
            }

            AssetPriceProvider provider = asset.getType() == AssetType.STOCK ? stockPriceProvider : cryptoPriceProvider;
            Optional<QuotedPrice> quote = provider.fetchPrice(asset.getSymbol());
            if (quote.isEmpty()) {
                failed++;
                continue;
            }

            BigDecimal priceInKrw = quote.get().price();
            if (!"KRW".equalsIgnoreCase(quote.get().currency())) {
                if (usdKrwRateCache[0] == null) {
                    usdKrwRateCache[0] = stockPriceProvider.fetchUsdKrwRate().orElse(null);
                }
                if (usdKrwRateCache[0] == null) {
                    failed++;
                    continue;
                }
                priceInKrw = priceInKrw.multiply(usdKrwRateCache[0]);
            }

            asset.updatePrice(priceInKrw, LocalDateTime.now());
            updated++;
        }

        return new RefreshResult(updated, failed);
    }
}
