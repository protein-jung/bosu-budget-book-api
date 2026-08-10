package com.bosu.housebook.asset;

import com.bosu.housebook.asset.dto.AssetRefreshResponse;
import com.bosu.housebook.asset.dto.AssetRequest;
import com.bosu.housebook.asset.dto.AssetResponse;
import com.bosu.housebook.asset.dto.AssetSummaryResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
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

    public AssetService(AssetRepository assetRepository, HouseholdRepository householdRepository,
            HouseholdService householdService, AssetPriceService assetPriceService) {
        this.assetRepository = assetRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.assetPriceService = assetPriceService;
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

        Asset asset = new Asset(household, request.type(), request.name(), request.custodian(), request.symbol(),
                request.quantity(), request.averagePrice(), request.manualValue(), request.memo(), request.address(),
                request.dong(), request.ho(), request.lawdCd(), request.complexName(), request.regionDongName());
        assetRepository.save(asset);
        return AssetResponse.from(asset);
    }

    @Transactional
    public AssetResponse update(Long userId, Long assetId, AssetRequest request) {
        Asset asset = getOwnedAsset(userId, assetId);
        validate(request);
        asset.update(request.type(), request.name(), request.custodian(), request.symbol(), request.quantity(),
                request.averagePrice(), request.manualValue(), request.memo(), request.address(), request.dong(),
                request.ho(), request.lawdCd(), request.complexName(), request.regionDongName());
        return AssetResponse.from(asset);
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

    private void validate(AssetRequest request) {
        if (request.type().isLivePriced()) {
            if (request.symbol() == null || request.symbol().isBlank()) {
                throw ApiException.badRequest("주식/코인은 심볼을 입력해야 합니다.");
            }
            if (request.quantity() == null || request.quantity().signum() <= 0) {
                throw ApiException.badRequest("주식/코인은 보유 수량을 입력해야 합니다.");
            }
        } else if (request.manualValue() == null || request.manualValue().signum() < 0) {
            throw ApiException.badRequest("평가금액을 입력해야 합니다.");
        }
    }

    private Asset getOwnedAsset(Long userId, Long assetId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return assetRepository.findByIdAndHouseholdId(assetId, householdId)
                .orElseThrow(() -> ApiException.notFound("자산을 찾을 수 없습니다."));
    }
}
