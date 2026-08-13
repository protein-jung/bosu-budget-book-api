package com.bosu.housebook.asset;

import com.bosu.housebook.asset.dto.AssetSnapshotResponse;
import com.bosu.housebook.asset.dto.AssetSummaryResponse;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 매일 자정(Asia/Seoul)에 가계부별 총자산 스냅샷을 하나씩 남긴다. 포트폴리오 화면의 일별 추이
 * 그래프가 이 기록을 읽어 보여준다. 같은 날짜에 여러 번 찍혀도(서버 재시작, 수동 보정 등) 덮어쓰기만
 * 하고 중복 행은 만들지 않는다(household_id+snapshot_date 유니크 제약과 findOrCreate 패턴). */
@Service
public class AssetSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(AssetSnapshotService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AssetSnapshotRepository snapshotRepository;
    private final AssetService assetService;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AssetSnapshotService(AssetSnapshotRepository snapshotRepository, AssetService assetService,
            HouseholdRepository householdRepository, HouseholdService householdService) {
        this.snapshotRepository = snapshotRepository;
        this.assetService = assetService;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void captureNightlySnapshots() {
        LocalDate today = LocalDate.now(KST);
        List<Household> households = householdRepository.findAll();
        for (Household household : households) {
            try {
                captureForHousehold(household.getId(), today);
            } catch (Exception e) {
                log.warn("자산 스냅샷 저장 실패: householdId={}, date={}", household.getId(), today, e);
            }
        }
    }

    @Transactional
    public void captureForHousehold(Long householdId, LocalDate date) {
        AssetService.HouseholdTotals totals = assetService.computeHouseholdTotals(householdId);
        String byTypeJson = toJson(totals.byType());
        AssetSnapshot existing = snapshotRepository.findByHouseholdIdAndSnapshotDate(householdId, date).orElse(null);
        if (existing == null) {
            Household household = householdRepository.getReferenceById(householdId);
            snapshotRepository.save(new AssetSnapshot(household, date, totals.total(), byTypeJson));
        } else {
            existing.update(totals.total(), byTypeJson);
        }
    }

    /** 조회할 때마다 "오늘" 스냅샷은 지금 값으로 다시 채워서(자정 이후 자산이 바뀌어도 오늘 점은
     * 계속 최신을 반영) 그래프 맨 끝이 항상 실시간에 가깝게 보이게 한다. 어제 이전 기록은 자정
     * 배치가 찍어둔 그대로 손대지 않는다. */
    @Transactional
    public List<AssetSnapshotResponse> getTrend(Long userId, int days) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        LocalDate today = LocalDate.now(KST);
        captureForHousehold(householdId, today);
        LocalDate from = today.minusDays(Math.max(days - 1, 0));
        return snapshotRepository
                .findByHouseholdIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(householdId, from)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AssetSnapshotResponse toResponse(AssetSnapshot snapshot) {
        return new AssetSnapshotResponse(snapshot.getSnapshotDate(), snapshot.getTotalValue(),
                fromJson(snapshot.getByTypeJson()));
    }

    private String toJson(Map<AssetType, BigDecimal> byType) {
        try {
            List<AssetSummaryResponse.TypeBreakdown> list = byType.entrySet().stream()
                    .map(e -> new AssetSummaryResponse.TypeBreakdown(e.getKey(), e.getValue()))
                    .sorted(Comparator.comparing(AssetSummaryResponse.TypeBreakdown::amount).reversed())
                    .toList();
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private List<AssetSummaryResponse.TypeBreakdown> fromJson(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AssetSummaryResponse.TypeBreakdown>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
