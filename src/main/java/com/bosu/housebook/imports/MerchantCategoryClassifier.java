package com.bosu.housebook.imports;

import com.bosu.housebook.merchantrule.MerchantCategoryRule;
import com.bosu.housebook.merchantrule.MerchantCategoryRuleRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 가맹점명 또는 쿠팡 상품명에 포함된 키워드로 대략적인 지출 카테고리를 추정한다. 규칙에 걸리지
 * 않으면 빈 Optional을 반환하고, 호출부에서 "미분류"로 처리한다. 실수로 잘못 묶이는 것보다
 * 애매하면 미분류로 남기는 쪽이 안전하므로 키워드는 비교적 구체적인 것만 사용한다.
 * <p>
 * 카테고리명은 엑셀 기반 기본 소분류(리프) 이름과 맞춘다.
 * <p>
 * 규칙은 모든 가계부가 공유하는 {@link MerchantCategoryRule} 테이블 하나로 공통 관리한다(코드에
 * 하드코딩하지 않음). {@link #classifyProduct(String)}는 같은 규칙을 그대로 사용하되, "우유",
 * "휴지"처럼 상품명에서만 쓰는 표현은 규칙별 productKeywords로 추가해 함께 매칭한다.
 */
@Component
public class MerchantCategoryClassifier {

    public record CategorySuggestion(String categoryName, String color, String icon) {
    }

    private final MerchantCategoryRuleRepository ruleRepository;

    public MerchantCategoryClassifier(MerchantCategoryRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Optional<CategorySuggestion> classify(String merchantName) {
        String upper = merchantName.toUpperCase(Locale.ROOT);
        return ruleRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(rule -> matchesAny(rule.getKeywords(), upper))
                .findFirst()
                .map(MerchantCategoryClassifier::toSuggestion);
    }

    /** 쿠팡 주문내역의 "상품명"으로 카테고리를 추정한다. */
    public Optional<CategorySuggestion> classifyProduct(String productName) {
        String upper = productName.toUpperCase(Locale.ROOT);
        return ruleRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(rule -> matchesAny(rule.getKeywords(), upper) || matchesAny(rule.getProductKeywords(), upper))
                .findFirst()
                .map(MerchantCategoryClassifier::toSuggestion);
    }

    private boolean matchesAny(List<String> keywords, String upperText) {
        return keywords.stream().anyMatch(upperText::contains);
    }

    private static CategorySuggestion toSuggestion(MerchantCategoryRule rule) {
        return new CategorySuggestion(rule.getCategoryName(), rule.getColor(), rule.getIcon());
    }
}
