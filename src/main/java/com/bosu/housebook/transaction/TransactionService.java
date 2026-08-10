package com.bosu.housebook.transaction;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.card.CardRepository;
import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.transaction.dto.TransactionRequest;
import com.bosu.housebook.transaction.dto.TransactionResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final CategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, HouseholdRepository householdRepository,
            HouseholdService householdService, CategoryRepository categoryRepository, CardRepository cardRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.categoryRepository = categoryRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public List<TransactionResponse> getMonthly(Long userId, int year, int month) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return transactionRepository
                .findByHouseholdIdAndTransactionDateBetweenAndIsBackfillFalseOrderByTransactionDateAscIdAsc(
                        householdId, from, to)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Household household = householdRepository.getReferenceById(householdId);
        Category category = getOwnedCategory(householdId, request.categoryId());
        Card card = getOwnedCard(householdId, request.cardId());
        User user = userRepository.getReferenceById(userId);

        Transaction transaction = new Transaction(household, request.type(), request.amount(),
                request.transactionDate(), category, card, user, request.memo());
        transactionRepository.save(transaction);
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long transactionId, TransactionRequest request) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Transaction transaction = transactionRepository.findByIdAndHouseholdId(transactionId, householdId)
                .orElseThrow(() -> ApiException.notFound("거래 내역을 찾을 수 없습니다."));
        Category category = getOwnedCategory(householdId, request.categoryId());
        Card card = getOwnedCard(householdId, request.cardId());

        transaction.update(request.type(), request.amount(), request.transactionDate(), category, card,
                request.memo());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Transaction transaction = transactionRepository.findByIdAndHouseholdId(transactionId, householdId)
                .orElseThrow(() -> ApiException.notFound("거래 내역을 찾을 수 없습니다."));
        transactionRepository.delete(transaction);
    }

    private Category getOwnedCategory(Long householdId, Long categoryId) {
        return categoryRepository.findByIdAndHouseholdId(categoryId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카테고리입니다."));
    }

    private Card getOwnedCard(Long householdId, Long cardId) {
        if (cardId == null) {
            return null;
        }
        return cardRepository.findByIdAndHouseholdId(cardId, householdId)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않은 카드입니다."));
    }
}
