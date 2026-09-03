package com.bosu.housebook.merchantrule;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.merchantrule.dto.MerchantCategoryRuleRequest;
import com.bosu.housebook.merchantrule.dto.MerchantCategoryRuleResponse;
import com.bosu.housebook.merchantrule.dto.UncategorizedMerchantResponse;
import com.bosu.housebook.transaction.TransactionRepository;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 설정 &gt; 명세서 가져오기의 "미분류 정리" 탭이 쓰는 서비스: 이 가계부의 미분류 거래를 가맹점명별로
 * 모아 보여주고, 사용자가 카테고리를 골라 이 가계부만의 분류 규칙을 등록·수정·삭제할 수 있게 한다.
 * 규칙을 새로 등록하거나 바꿔도 이미 미분류로 들어간 기존 거래는 건드리지 않는다 — 다음
 * 가져오기부터만 적용된다. */
@Service
@Transactional(readOnly = true)
public class MerchantCategoryRuleService {

    private static final String UNCATEGORIZED_NAME = "미분류";

    private final HouseholdService householdService;
    private final HouseholdRepository householdRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantCategoryRuleRepository ruleRepository;

    public MerchantCategoryRuleService(HouseholdService householdService, HouseholdRepository householdRepository,
            CategoryRepository categoryRepository, TransactionRepository transactionRepository,
            MerchantCategoryRuleRepository ruleRepository) {
        this.householdService = householdService;
        this.householdRepository = householdRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.ruleRepository = ruleRepository;
    }

    public List<MerchantCategoryRuleResponse> getRules(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return ruleRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(householdId).stream()
                .map(MerchantCategoryRuleResponse::from)
                .toList();
    }

    public List<UncategorizedMerchantResponse> getUncategorizedMerchants(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        List<Long> uncategorizedCategoryIds = Stream.of(TransactionType.EXPENSE, TransactionType.INCOME)
                .flatMap(type -> categoryRepository
                        .findByHouseholdIdAndNameAndType(householdId, UNCATEGORIZED_NAME, type).stream())
                .map(Category::getId)
                .toList();
        if (uncategorizedCategoryIds.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findUncategorizedMerchantSummaries(householdId, uncategorizedCategoryIds).stream()
                .map(UncategorizedMerchantResponse::from)
                .toList();
    }

    @Transactional
    public MerchantCategoryRuleResponse createRule(Long userId, MerchantCategoryRuleRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        Category category = requireOwnedCategory(householdId, request.categoryId());
        List<String> keywords = normalizeKeywords(request.keywords());

        int nextSortOrder = (int) ruleRepository.countByHouseholdId(householdId);
        MerchantCategoryRule rule = ruleRepository.save(
                new MerchantCategoryRule(household, category, keywords, List.of(), nextSortOrder));
        return MerchantCategoryRuleResponse.from(rule);
    }

    @Transactional
    public MerchantCategoryRuleResponse updateRule(Long userId, Long ruleId, MerchantCategoryRuleRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        MerchantCategoryRule rule = ruleRepository.findByIdAndHouseholdId(ruleId, householdId)
                .orElseThrow(() -> ApiException.notFound("규칙을 찾을 수 없습니다."));
        Category category = requireOwnedCategory(householdId, request.categoryId());
        List<String> keywords = normalizeKeywords(request.keywords());

        rule.update(category, keywords);
        return MerchantCategoryRuleResponse.from(rule);
    }

    @Transactional
    public void deleteRule(Long userId, Long ruleId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        MerchantCategoryRule rule = ruleRepository.findByIdAndHouseholdId(ruleId, householdId)
                .orElseThrow(() -> ApiException.notFound("규칙을 찾을 수 없습니다."));
        ruleRepository.delete(rule);
    }

    private Category requireOwnedCategory(Long householdId, Long categoryId) {
        return categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카테고리입니다."));
    }

    private List<String> normalizeKeywords(List<String> rawKeywords) {
        List<String> keywords = rawKeywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
        if (keywords.isEmpty()) {
            throw ApiException.badRequest("키워드를 입력해주세요.");
        }
        return keywords;
    }
}
