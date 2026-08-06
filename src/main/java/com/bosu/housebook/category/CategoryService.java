package com.bosu.housebook.category;

import com.bosu.housebook.category.dto.CategoryRequest;
import com.bosu.housebook.category.dto.CategoryResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;

    public CategoryService(CategoryRepository categoryRepository, HouseholdRepository householdRepository,
            HouseholdService householdService) {
        this.categoryRepository = categoryRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
    }

    public List<CategoryResponse> getAll(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return categoryRepository.findByHouseholdIdOrderByIdAsc(householdId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        Category category = new Category(household, request.name(), request.type(), request.color());
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);
        category.update(request.name(), request.color());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = getOwnedCategory(userId, categoryId);
        categoryRepository.delete(category);
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
    }
}
