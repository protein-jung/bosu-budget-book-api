package com.bosu.housebook.statistics;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.statistics.dto.CardStat;
import com.bosu.housebook.statistics.dto.CategoryStat;
import com.bosu.housebook.statistics.dto.MemberStat;
import com.bosu.housebook.statistics.dto.MonthlySummaryResponse;
import com.bosu.housebook.transaction.Transaction;
import com.bosu.housebook.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final TransactionRepository transactionRepository;
    private final HouseholdService householdService;

    public StatisticsService(TransactionRepository transactionRepository, HouseholdService householdService) {
        this.transactionRepository = transactionRepository;
        this.householdService = householdService;
    }

    public MonthlySummaryResponse getMonthlySummary(Long userId, int year, int month) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        List<Transaction> transactions = transactionRepository
                .findByHouseholdIdAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(householdId, from, to);

        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);

        return new MonthlySummaryResponse(totalIncome, totalExpense, byCategory(transactions), byCard(transactions),
                byMember(transactions));
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryStat> byCategory(List<Transaction> transactions) {
        Map<Long, CategoryStat> stats = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            var category = t.getCategory();
            stats.merge(category.getId(),
                    new CategoryStat(category.getId(), category.getName(), category.getColor(), t.getType(),
                            t.getAmount()),
                    (existing, added) -> new CategoryStat(existing.categoryId(), existing.categoryName(),
                            existing.color(), existing.type(), existing.amount().add(added.amount())));
        }
        return stats.values().stream()
                .sorted(Comparator.comparing(CategoryStat::amount).reversed())
                .toList();
    }

    private List<CardStat> byCard(List<Transaction> transactions) {
        Map<Long, CardStat> stats = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            var card = t.getCard();
            if (card == null) {
                continue;
            }
            stats.merge(card.getId(), new CardStat(card.getId(), card.getName(), t.getAmount()),
                    (existing, added) -> new CardStat(existing.cardId(), existing.cardName(),
                            existing.amount().add(added.amount())));
        }
        return stats.values().stream()
                .sorted(Comparator.comparing(CardStat::amount).reversed())
                .toList();
    }

    private List<MemberStat> byMember(List<Transaction> transactions) {
        Map<Long, MemberStat> stats = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            var user = t.getUser();
            BigDecimal income = t.getType() == TransactionType.INCOME ? t.getAmount() : BigDecimal.ZERO;
            BigDecimal expense = t.getType() == TransactionType.EXPENSE ? t.getAmount() : BigDecimal.ZERO;
            stats.merge(user.getId(), new MemberStat(user.getId(), user.getName(), income, expense),
                    (existing, added) -> new MemberStat(existing.userId(), existing.userName(),
                            existing.income().add(added.income()), existing.expense().add(added.expense())));
        }
        return List.copyOf(stats.values());
    }
}
