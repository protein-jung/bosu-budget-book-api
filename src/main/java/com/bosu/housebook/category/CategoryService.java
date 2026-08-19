package com.bosu.housebook.category;

import com.bosu.housebook.category.dto.CategoryMemoResponse;
import com.bosu.housebook.category.dto.CategoryRequest;
import com.bosu.housebook.category.dto.CategoryResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMemoRepository categoryMemoRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final CategoryDefaultSeeder categoryDefaultSeeder;

    public CategoryService(CategoryRepository categoryRepository, CategoryMemoRepository categoryMemoRepository,
            HouseholdRepository householdRepository, HouseholdService householdService,
            CategoryDefaultSeeder categoryDefaultSeeder) {
        this.categoryRepository = categoryRepository;
        this.categoryMemoRepository = categoryMemoRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.categoryDefaultSeeder = categoryDefaultSeeder;
    }

    @Transactional
    public List<CategoryResponse> getAll(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        ensureDefaults(householdId);
        return categoryRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(householdId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        if (request.isGroup() && request.parentId() != null) {
            throw ApiException.badRequest("상위 카테고리는 다른 카테고리의 하위로 지정할 수 없습니다.");
        }
        Category parent = resolveParent(householdId, request.parentId(), request.type());
        Category category = new Category(household, request.name(), request.type(), request.color(),
                request.icon(), parent, 0, request.targetAmount(), request.isGroup());
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);
        if (request.parentId() != null && request.parentId().equals(categoryId)) {
            throw ApiException.badRequest("자기 자신을 상위 카테고리로 지정할 수 없습니다.");
        }
        if (request.isGroup() && request.parentId() != null) {
            throw ApiException.badRequest("상위 카테고리는 다른 카테고리의 하위로 지정할 수 없습니다.");
        }
        Category parent = resolveParent(category.getHousehold().getId(), request.parentId(), category.getType());
        category.update(request.name(), request.color(), request.icon(), request.targetAmount(), request.isGroup());
        category.updateParent(parent);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void reorder(Long userId, List<Long> categoryIds) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw ApiException.notFound("카테고리를 찾을 수 없습니다.");
        }
        Map<Long, Category> byId = categories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Long expectedParentId = null;
        for (int i = 0; i < categoryIds.size(); i++) {
            Category category = byId.get(categoryIds.get(i));
            if (category == null || !category.getHousehold().getId().equals(householdId)) {
                throw ApiException.notFound("카테고리를 찾을 수 없습니다.");
            }
            Long parentId = category.getParent() == null ? null : category.getParent().getId();
            if (i == 0) {
                expectedParentId = parentId;
            } else if (!Objects.equals(expectedParentId, parentId)) {
                throw ApiException.badRequest("같은 상위 카테고리 내에서만 순서를 변경할 수 있습니다.");
            }
        }

        for (int i = 0; i < categoryIds.size(); i++) {
            byId.get(categoryIds.get(i)).updateSortOrder(i);
        }
    }

    @Transactional
    public List<CategoryMemoResponse> getAllMemos(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return categoryMemoRepository.findByCategoryHouseholdId(householdId).stream()
                .map(CategoryMemoResponse::from)
                .toList();
    }

    @Transactional
    public CategoryMemoResponse setMemo(Long userId, Long categoryId, int year, int month, String memo) {
        Category category = getOwnedCategory(userId, categoryId);
        CategoryMemo entity = categoryMemoRepository.findByCategoryIdAndYearAndMonth(categoryId, year, month)
                .orElseGet(() -> categoryMemoRepository.save(new CategoryMemo(category, year, month, memo)));
        entity.updateMemo(memo);
        return CategoryMemoResponse.from(entity);
    }

    @Transactional
    public void clearMemo(Long userId, Long categoryId, int year, int month) {
        getOwnedCategory(userId, categoryId);
        categoryMemoRepository.deleteByCategoryIdAndYearAndMonth(categoryId, year, month);
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = getOwnedCategory(userId, categoryId);
        Long householdId = category.getHousehold().getId();
        if (categoryRepository.existsByHouseholdIdAndParentId(householdId, categoryId)) {
            throw ApiException.badRequest("하위 카테고리가 있어 삭제할 수 없습니다.");
        }
        categoryRepository.delete(category);
    }

    private void ensureDefaults(Long householdId) {
        if (!categoryRepository.existsByHouseholdId(householdId)) {
            Household household = householdRepository.getReferenceById(householdId);
            categoryDefaultSeeder.seed(household);
        }
    }

    private Category resolveParent(Long householdId, Long parentId, com.bosu.housebook.common.TransactionType type) {
        if (parentId == null) {
            return null;
        }
        Category parent = categoryRepository.findByIdAndHouseholdId(parentId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 상위 카테고리입니다."));
        if (parent.getType() != type) {
            throw ApiException.badRequest("상위 카테고리와 유형이 같아야 합니다.");
        }
        if (parent.getParent() != null) {
            throw ApiException.badRequest("상위 카테고리는 대분류만 지정할 수 있습니다.");
        }
        return parent;
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
    }
}
