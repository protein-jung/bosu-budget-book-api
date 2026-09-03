package com.bosu.housebook.merchantrule;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 가계부 생성 시 {@link DefaultMerchantCategoryRuleTemplate} 테이블(모든 가계부가 공유하는
 * 공통 기본 규칙)을 읽어 household 소유의 가맹점 분류 규칙으로 복제한다. 템플릿의 카테고리명은
 * {@code CategoryDefaultSeeder}가 먼저 심어둔 기본 카테고리 트리와 이름이 맞게 관리되므로, 그
 * 이름으로 이 household의 실제 지출 카테고리를 찾아 연결한다(못 찾으면 그 규칙은 건너뛴다). */
@Component
public class MerchantRuleDefaultSeeder {

    private final MerchantCategoryRuleRepository ruleRepository;
    private final DefaultMerchantCategoryRuleTemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;

    public MerchantRuleDefaultSeeder(MerchantCategoryRuleRepository ruleRepository,
            DefaultMerchantCategoryRuleTemplateRepository templateRepository, CategoryRepository categoryRepository) {
        this.ruleRepository = ruleRepository;
        this.templateRepository = templateRepository;
        this.categoryRepository = categoryRepository;
    }

    public void seed(Household household) {
        List<DefaultMerchantCategoryRuleTemplate> templates = templateRepository.findAllByOrderBySortOrderAscIdAsc();
        for (DefaultMerchantCategoryRuleTemplate template : templates) {
            Optional<Category> category = categoryRepository
                    .findByHouseholdIdAndNameAndType(household.getId(), template.getCategoryName(), TransactionType.EXPENSE)
                    .stream()
                    .findFirst();
            category.ifPresent(c -> ruleRepository.save(new MerchantCategoryRule(household, c, template.getKeywords(),
                    template.getProductKeywords(), template.getSortOrder())));
        }
    }
}
