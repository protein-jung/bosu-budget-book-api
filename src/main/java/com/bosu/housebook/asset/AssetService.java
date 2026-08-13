package com.bosu.housebook.asset;

import com.bosu.housebook.asset.dto.AssetRefreshResponse;
import com.bosu.housebook.asset.dto.AssetRequest;
import com.bosu.housebook.asset.dto.AssetResponse;
import com.bosu.housebook.asset.dto.AssetSummaryResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetService {

    private static final String UNSPECIFIED_CUSTODIAN = "미지정";

    private final AssetRepository assetRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final AssetPriceService assetPriceService;
    private final UserRepository userRepository;
    private final YahooStockPriceProvider stockPriceProvider;

    public AssetService(AssetRepository assetRepository, HouseholdRepository householdRepository,
            HouseholdService householdService, AssetPriceService assetPriceService, UserRepository userRepository,
            YahooStockPriceProvider stockPriceProvider) {
        this.assetRepository = assetRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.assetPriceService = assetPriceService;
        this.userRepository = userRepository;
        this.stockPriceProvider = stockPriceProvider;
    }

    public List<AssetResponse> getAll(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return assetRepository.findByHouseholdIdOrderByIdAsc(householdId).stream()
                .map(AssetResponse::from)
                .toList();
    }

    @Transactional
    public AssetResponse create(Long userId, AssetRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        validate(request);
        BigDecimal averagePrice = resolveAveragePrice(request);
        // 소유주는 따로 고르지 않고 등록한 사람으로 자동 지정한다.
        User owner = userRepository.getReferenceById(userId);

        Asset asset = new Asset(household, request.type(), request.name(), request.custodian(), request.symbol(),
                request.quantity(), averagePrice, request.manualValue(), request.memo(), request.address(),
                request.dong(), request.ho(), request.lawdCd(), request.complexName(), request.regionDongName(),
                request.accountCategory(), owner, request.cashCategory(), request.maturityDate(),
                request.cashInterestRate(), request.cashStartDate(), request.purchaseDate(), request.encarUrl(),
                request.loanPrincipal(), request.loanStartMonth(), request.loanTermMonths(),
                request.loanMonthlyPayment(), request.loanInterestRate(), request.loanRepaymentType());
        assetRepository.save(asset);
        return AssetResponse.from(asset);
    }

    @Transactional
    public AssetResponse update(Long userId, Long assetId, AssetRequest request) {
        Asset asset = getOwnedAsset(userId, assetId);
        validate(request);
        BigDecimal averagePrice = resolveAveragePrice(request);
        asset.update(request.type(), request.name(), request.custodian(), request.symbol(), request.quantity(),
                averagePrice, request.manualValue(), request.memo(), request.address(), request.dong(),
                request.ho(), request.lawdCd(), request.complexName(), request.regionDongName(),
                request.accountCategory(), request.cashCategory(), request.maturityDate(),
                request.cashInterestRate(), request.cashStartDate(), request.purchaseDate(), request.encarUrl(),
                request.loanPrincipal(), request.loanStartMonth(), request.loanTermMonths(),
                request.loanMonthlyPayment(), request.loanInterestRate(), request.loanRepaymentType());
        return AssetResponse.from(asset);
    }

    /** averagePriceCurrency가 USD면 저장 시점 환율로 원화 환산한다. STOCK이 아니거나 통화가 없으면 그대로 둔다. */
    private BigDecimal resolveAveragePrice(AssetRequest request) {
        BigDecimal averagePrice = request.averagePrice();
        if (averagePrice == null || request.type() != AssetType.STOCK
                || request.averagePriceCurrency() != PriceCurrency.USD) {
            return averagePrice;
        }
        BigDecimal usdKrwRate = stockPriceProvider.fetchUsdKrwRate()
                .orElseThrow(() -> ApiException.badRequest("환율 조회에 실패했습니다. 잠시 후 다시 시도해주세요."));
        return averagePrice.multiply(usdKrwRate);
    }

    @Transactional
    public void delete(Long userId, Long assetId) {
        Asset asset = getOwnedAsset(userId, assetId);
        assetRepository.delete(asset);
    }

    @Transactional
    public AssetRefreshResponse refreshPrices(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        List<Asset> assets = assetRepository.findByHouseholdIdOrderByIdAsc(householdId);
        AssetPriceService.RefreshResult result = assetPriceService.refreshPrices(assets);
        List<AssetResponse> responses = assets.stream().map(AssetResponse::from).toList();
        return new AssetRefreshResponse(result.updatedCount(), result.failedCount(), responses);
    }

    public AssetSummaryResponse getSummary(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        List<Asset> assets = assetRepository.findByHouseholdIdOrderByIdAsc(householdId);

        BigDecimal total = BigDecimal.ZERO;
        Map<AssetType, BigDecimal> byType = new LinkedHashMap<>();
        Map<String, BigDecimal> byCustodian = new LinkedHashMap<>();
        for (Asset asset : assets) {
            BigDecimal value = asset.getCurrentValue() != null ? asset.getCurrentValue() : BigDecimal.ZERO;
            total = total.add(value);
            byType.merge(asset.getType(), value, BigDecimal::add);
            String custodian = asset.getCustodian() == null || asset.getCustodian().isBlank()
                    ? UNSPECIFIED_CUSTODIAN
                    : asset.getCustodian();
            byCustodian.merge(custodian, value, BigDecimal::add);
        }

        List<AssetSummaryResponse.TypeBreakdown> typeBreakdown = byType.entrySet().stream()
                .map(e -> new AssetSummaryResponse.TypeBreakdown(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(AssetSummaryResponse.TypeBreakdown::amount).reversed())
                .toList();
        List<AssetSummaryResponse.CustodianBreakdown> custodianBreakdown = byCustodian.entrySet().stream()
                .map(e -> new AssetSummaryResponse.CustodianBreakdown(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(AssetSummaryResponse.CustodianBreakdown::amount).reversed())
                .toList();

        return new AssetSummaryResponse(total, typeBreakdown, custodianBreakdown);
    }

    /** 스냅샷 배치용: 가계부의 자산 총액 + 타입별 합계만 계산한다(보관처별 집계는 필요 없음). */
    public HouseholdTotals computeHouseholdTotals(Long householdId) {
        List<Asset> assets = assetRepository.findByHouseholdIdOrderByIdAsc(householdId);
        BigDecimal total = BigDecimal.ZERO;
        Map<AssetType, BigDecimal> byType = new LinkedHashMap<>();
        for (Asset asset : assets) {
            BigDecimal value = asset.getCurrentValue() != null ? asset.getCurrentValue() : BigDecimal.ZERO;
            total = total.add(value);
            byType.merge(asset.getType(), value, BigDecimal::add);
        }
        return new HouseholdTotals(total, byType);
    }

    public record HouseholdTotals(BigDecimal total, Map<AssetType, BigDecimal> byType) {
    }

    private void validate(AssetRequest request) {
        if (request.type() == AssetType.LOAN) {
            validateLoan(request);
            return;
        }
        boolean hasSymbol = request.symbol() != null && !request.symbol().isBlank();
        if (request.type().isLivePriced() && hasSymbol) {
            if (request.quantity() == null || request.quantity().signum() <= 0) {
                throw ApiException.badRequest("주식/코인은 보유 수량을 입력해야 합니다.");
            }
        } else if (request.manualValue() == null || request.manualValue().signum() < 0) {
            throw ApiException.badRequest("평가금액을 입력해야 합니다.");
        }
    }

    private void validateLoan(AssetRequest request) {
        if (request.loanPrincipal() == null || request.loanPrincipal().signum() < 0) {
            throw ApiException.badRequest("대출 원금을 입력해야 합니다.");
        }
        if (request.loanStartMonth() == null) {
            throw ApiException.badRequest("대출 시작년월을 입력해야 합니다.");
        }
        if (request.loanTermMonths() == null || request.loanTermMonths() <= 0) {
            throw ApiException.badRequest("상환 기한을 입력해야 합니다.");
        }
        if (request.loanMonthlyPayment() == null || request.loanMonthlyPayment().signum() < 0) {
            throw ApiException.badRequest("월 납입금을 입력해야 합니다.");
        }
        if (request.loanInterestRate() == null || request.loanInterestRate().signum() < 0) {
            throw ApiException.badRequest("이율을 입력해야 합니다.");
        }
    }

    private Asset getOwnedAsset(Long userId, Long assetId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return assetRepository.findByIdAndHouseholdId(assetId, householdId)
                .orElseThrow(() -> ApiException.notFound("자산을 찾을 수 없습니다."));
    }
}
