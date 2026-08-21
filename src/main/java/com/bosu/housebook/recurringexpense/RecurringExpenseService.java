package com.bosu.housebook.recurringexpense;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.recurringexpense.dto.RecurringExpenseRequest;
import com.bosu.housebook.recurringexpense.dto.RecurringExpenseResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public RecurringExpenseService(RecurringExpenseRepository recurringExpenseRepository,
            HouseholdRepository householdRepository, HouseholdService householdService,
            CategoryRepository categoryRepository, UserRepository userRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<RecurringExpenseResponse> getAll(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        return recurringExpenseRepository.findByHouseholdIdOrderByDayOfMonthAscIdAsc(householdId).stream()
                .map(RecurringExpenseResponse::from)
                .toList();
    }

    @Transactional
    public RecurringExpenseResponse create(Long userId, RecurringExpenseRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        Category category = getOwnedExpenseCategory(householdId, request.categoryId());
        User createdBy = userRepository.getReferenceById(userId);

        RecurringExpense recurringExpense = new RecurringExpense(household, category, createdBy, request.name(),
                request.amount(), request.dayOfMonth(), request.active(), normalizeMemo(request.memo()));
        recurringExpenseRepository.save(recurringExpense);
        return RecurringExpenseResponse.from(recurringExpense);
    }

    @Transactional
    public RecurringExpenseResponse update(Long userId, Long recurringExpenseId, RecurringExpenseRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        RecurringExpense recurringExpense = getOwned(householdId, recurringExpenseId);
        Category category = getOwnedExpenseCategory(householdId, request.categoryId());

        recurringExpense.update(category, request.name(), request.amount(), request.dayOfMonth(), request.active(),
                normalizeMemo(request.memo()));
        return RecurringExpenseResponse.from(recurringExpense);
    }

    @Transactional
    public RecurringExpenseResponse setActive(Long userId, Long recurringExpenseId, boolean active) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        RecurringExpense recurringExpense = getOwned(householdId, recurringExpenseId);
        recurringExpense.updateActive(active);
        return RecurringExpenseResponse.from(recurringExpense);
    }

    @Transactional
    public void delete(Long userId, Long recurringExpenseId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        RecurringExpense recurringExpense = getOwned(householdId, recurringExpenseId);
        recurringExpenseRepository.delete(recurringExpense);
    }

    private RecurringExpense getOwned(Long householdId, Long recurringExpenseId) {
        return recurringExpenseRepository.findByIdAndHouseholdId(recurringExpenseId, householdId)
                .orElseThrow(() -> ApiException.notFound("고정비 지출을 찾을 수 없습니다."));
    }

    private Category getOwnedExpenseCategory(Long householdId, Long categoryId) {
        Category category = categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카테고리입니다."));
        if (category.getType() != TransactionType.EXPENSE) {
            throw ApiException.badRequest("지출 카테고리만 선택할 수 있습니다.");
        }
        return category;
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.trim();
    }
}
