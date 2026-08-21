package com.bosu.housebook.category;

import com.bosu.housebook.household.Household;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 가계부 생성 시 {@link DefaultCategoryTemplate} 테이블(모든 가계부가 공유하는 공통 기본
 * 카테고리 트리)을 읽어 household 소유의 카테고리로 복제한다. */
@Component
public class CategoryDefaultSeeder {

    private final CategoryRepository categoryRepository;
    private final DefaultCategoryTemplateRepository templateRepository;

    public CategoryDefaultSeeder(CategoryRepository categoryRepository,
            DefaultCategoryTemplateRepository templateRepository) {
        this.categoryRepository = categoryRepository;
        this.templateRepository = templateRepository;
    }

    public void seed(Household household) {
        List<DefaultCategoryTemplate> templates = templateRepository.findAllByOrderBySortOrderAscIdAsc();

        Map<Long, Category> savedByTemplateId = new HashMap<>();
        for (DefaultCategoryTemplate template : templates) {
            if (template.getParentId() != null) {
                continue;
            }
            Category saved = categoryRepository.save(new Category(
                    household, template.getName(), template.getType(), template.getColor(), template.getIcon(),
                    null, template.getSortOrder(), null, template.isGroup(), template.isExcludedFromExpenseStats()));
            savedByTemplateId.put(template.getId(), saved);
        }

        for (DefaultCategoryTemplate template : templates) {
            if (template.getParentId() == null) {
                continue;
            }
            Category parent = savedByTemplateId.get(template.getParentId());
            categoryRepository.save(new Category(
                    household, template.getName(), template.getType(), template.getColor(), template.getIcon(),
                    parent, template.getSortOrder(), null, false, template.isExcludedFromExpenseStats()));
        }
    }
}
