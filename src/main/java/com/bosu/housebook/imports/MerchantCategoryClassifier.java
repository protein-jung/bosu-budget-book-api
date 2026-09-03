package com.bosu.housebook.imports;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.merchantrule.MerchantCategoryRule;
import com.bosu.housebook.merchantrule.MerchantCategoryRuleRepository;
import com.bosu.housebook.merchantrule.MerchantRuleDefaultSeeder;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 가맹점명 또는 쿠팡 상품명에 포함된 키워드로 대략적인 지출 카테고리를 추정한다. 규칙에 걸리지
 * 않으면 빈 Optional을 반환하고, 호출부에서 "미분류"로 처리한다. 실수로 잘못 묶이는 것보다
 * 애매하면 미분류로 남기는 쪽이 안전하므로 키워드는 비교적 구체적인 것만 사용한다.
 * <p>
 * 규칙은 {@link MerchantCategoryRule} 테이블에 household 소유로, 실제 {@link Category}를
 * 가리키는 형태로 저장된다(가계부마다 다른 규칙을 가질 수 있음).
 * {@link #classifyProduct(String, Household)}는 같은 규칙을 그대로 사용하되, "우유", "휴지"처럼
 * 상품명에서만 쓰는 표현은 규칙별 productKeywords로 추가해 함께 매칭한다.
 */
@Component
public class MerchantCategoryClassifier {

    private final MerchantCategoryRuleRepository ruleRepository;
    private final MerchantRuleDefaultSeeder defaultSeeder;

    public MerchantCategoryClassifier(MerchantCategoryRuleRepository ruleRepository,
            MerchantRuleDefaultSeeder defaultSeeder) {
        this.ruleRepository = ruleRepository;
        this.defaultSeeder = defaultSeeder;
    }

    public Optional<Category> classify(String merchantName, Household household) {
        String upper = merchantName.toUpperCase(Locale.ROOT);
        return rulesFor(household).stream()
                .filter(rule -> matchesAny(rule.getKeywords(), upper))
                .findFirst()
                .map(MerchantCategoryRule::getCategory);
    }

    /** 쿠팡 주문내역의 "상품명"으로 카테고리를 추정한다. */
    public Optional<Category> classifyProduct(String productName, Household household) {
        String upper = productName.toUpperCase(Locale.ROOT);
        return rulesFor(household).stream()
                .filter(rule -> matchesAny(rule.getKeywords(), upper) || matchesAny(rule.getProductKeywords(), upper))
                .findFirst()
                .map(MerchantCategoryRule::getCategory);
    }

    /** V33 이전에 만들어져 아직 자기 규칙이 없는 가계부를 방어적으로 시딩한다(정상적으로는
     * {@code HouseholdService.create}에서 생성 시점에 이미 채워진다). */
    private List<MerchantCategoryRule> rulesFor(Household household) {
        if (!ruleRepository.existsByHouseholdId(household.getId())) {
            defaultSeeder.seed(household);
        }
        return ruleRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(household.getId());
    }

    private boolean matchesAny(List<String> keywords, String upperText) {
        return keywords.stream().anyMatch(upperText::contains);
    }
}
