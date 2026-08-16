package com.bosu.housebook.statistics;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.category.CategoryMonthlyTarget;
import com.bosu.housebook.category.CategoryMonthlyTargetRepository;
import com.bosu.housebook.category.CategoryRepository;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.HouseholdService;
import com.bosu.housebook.statistics.dto.CardStat;
import com.bosu.housebook.statistics.dto.CategoryBudget;
import com.bosu.housebook.statistics.dto.CategoryStat;
import com.bosu.housebook.statistics.dto.MemberStat;
import com.bosu.housebook.statistics.dto.MonthlySummaryResponse;
import com.bosu.housebook.statistics.dto.MonthlyTrendPoint;
import com.bosu.housebook.statistics.dto.RangeSummaryResponse;
import com.bosu.housebook.transaction.Transaction;
import com.bosu.housebook.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private static final int MAX_RANGE_MONTHS = 24;

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMonthlyTargetRepository categoryMonthlyTargetRepository;
    private final HouseholdService householdService;

    public StatisticsService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
            CategoryMonthlyTargetRepository categoryMonthlyTargetRepository, HouseholdService householdService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.categoryMonthlyTargetRepository = categoryMonthlyTargetRepository;
        this.householdService = householdService;
    }

    public MonthlySummaryResponse getMonthlySummary(Long userId, int year, int month) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Transaction> transactions = loadTransactions(householdId, yearMonth, yearMonth);
        List<Category> categories = categoryRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(householdId);
        return buildMonthlySummary(householdId, categories, year, month, transactions);
    }

    public RangeSummaryResponse getRangeSummary(Long userId, int fromYear, int fromMonth, int toYear, int toMonth) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        YearMonth from = YearMonth.of(fromYear, fromMonth);
        YearMonth to = YearMonth.of(toYear, toMonth);
        if (to.isBefore(from)) {
            throw ApiException.badRequest("종료 월이 시작 월보다 빠를 수 없습니다.");
        }
        long months = ChronoUnit.MONTHS.between(from, to) + 1;
        if (months > MAX_RANGE_MONTHS) {
            throw ApiException.badRequest("조회 기간은 최대 " + MAX_RANGE_MONTHS + "개월입니다.");
        }
        return buildRangeSummary(householdId, from, to);
    }

    /** 이 가계부에 처음 거래가 기록된 달부터 이번 달까지의 월별 카테고리별 내역을 반환한다. */
    public RangeSummaryResponse getFullHistorySummary(Long userId) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        Optional<Transaction> earliest = transactionRepository
                .findFirstByHouseholdIdOrderByTransactionDateAsc(householdId);
        if (earliest.isEmpty()) {
            return new RangeSummaryResponse(List.of());
        }

        YearMonth from = YearMonth.from(earliest.get().getTransactionDate());
        YearMonth to = YearMonth.now();
        long months = ChronoUnit.MONTHS.between(from, to) + 1;
        if (months > MAX_RANGE_MONTHS) {
            from = to.minusMonths(MAX_RANGE_MONTHS - 1);
        }
        return buildRangeSummary(householdId, from, to);
    }

    private RangeSummaryResponse buildRangeSummary(Long householdId, YearMonth from, YearMonth to) {
        List<Transaction> transactions = loadTransactions(householdId, from, to);
        List<Category> categories = categoryRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(householdId);
        Map<YearMonth, List<Transaction>> byMonth = new LinkedHashMap<>();
        for (YearMonth cursor = from; !cursor.isAfter(to); cursor = cursor.plusMonths(1)) {
            byMonth.put(cursor, new ArrayList<>());
        }
        for (Transaction t : transactions) {
            YearMonth key = YearMonth.from(t.getTransactionDate());
            List<Transaction> bucket = byMonth.get(key);
            if (bucket != null) {
                bucket.add(t);
            }
        }

        List<MonthlyTrendPoint> points = byMonth.entrySet().stream()
                .map(entry -> {
                    MonthlySummaryResponse summary = buildMonthlySummary(householdId, categories,
                            entry.getKey().getYear(), entry.getKey().getMonthValue(), entry.getValue());
                    return new MonthlyTrendPoint(
                            entry.getKey().getYear(),
                            entry.getKey().getMonthValue(),
                            summary.totalIncome(),
                            summary.totalExpense(),
                            summary.netAmount(),
                            summary.byCategory(),
                            summary.byParentCategory());
                })
                .toList();
        return new RangeSummaryResponse(points);
    }

    private List<Transaction> loadTransactions(Long householdId, YearMonth from, YearMonth to) {
        LocalDate fromDate = from.atDay(1);
        LocalDate toDate = to.atEndOfMonth();
        return transactionRepository
                .findByHouseholdIdAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                        householdId, fromDate, toDate);
    }

    private MonthlySummaryResponse buildMonthlySummary(Long householdId, List<Category> categories, int year,
            int month, List<Transaction> transactions) {
        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);
        List<CategoryStat> byCategoryList = byCategory(transactions);
        return new MonthlySummaryResponse(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                byCategoryList,
                byParentCategory(transactions),
                byCard(transactions),
                byMember(transactions),
                buildBudgets(householdId, categories, year, month, byCategoryList));
    }

    private List<CategoryBudget> buildBudgets(Long householdId, List<Category> categories, int year, int month,
            List<CategoryStat> byCategoryList) {
        Map<Long, BigDecimal> overrides = categoryMonthlyTargetRepository
                .findByCategoryHouseholdIdAndYearAndMonth(householdId, year, month).stream()
                .collect(Collectors.toMap(t -> t.getCategory().getId(), CategoryMonthlyTarget::getAmount));
        Map<Long, BigDecimal> spentByCategory = byCategoryList.stream()
                .collect(Collectors.toMap(CategoryStat::categoryId, CategoryStat::amount));

        List<CategoryBudget> budgets = new ArrayList<>();
        for (Category category : categories) {
            boolean overridden = overrides.containsKey(category.getId());
            BigDecimal target = overridden ? overrides.get(category.getId()) : category.getTargetAmount();
            if (target == null) {
                continue;
            }
            BigDecimal spent = spentByCategory.getOrDefault(category.getId(), BigDecimal.ZERO);
            budgets.add(new CategoryBudget(category.getId(), category.getName(), category.getColor(),
                    category.getIcon(), category.getType(), target, spent, overridden));
        }
        return budgets;
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
            Category category = t.getCategory();
            Category parent = category.getParent();
            Long parentId = parent == null ? null : parent.getId();
            String parentName = parent == null ? null : parent.getName();
            stats.merge(
                    category.getId(),
                    new CategoryStat(
                            category.getId(),
                            category.getName(),
                            category.getColor(),
                            category.getIcon(),
                            t.getType(),
                            t.getAmount(),
                            parentId,
                            parentName),
                    (existing, added) -> new CategoryStat(
                            existing.categoryId(),
                            existing.categoryName(),
                            existing.color(),
                            existing.icon(),
                            existing.type(),
                            existing.amount().add(added.amount()),
                            existing.parentId(),
                            existing.parentName()));
        }
        return stats.values().stream()
                .sorted(Comparator.comparing(CategoryStat::amount).reversed())
                .toList();
    }

    private List<CategoryStat> byParentCategory(List<Transaction> transactions) {
        Map<String, CategoryStat> stats = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            Category category = t.getCategory();
            Category parent = category.getParent();
            Long groupId = parent == null ? category.getId() : parent.getId();
            String groupName = parent == null ? category.getName() : parent.getName();
            String groupColor = parent == null ? category.getColor() : parent.getColor();
            String groupIcon = parent == null ? category.getIcon() : parent.getIcon();
            String key = t.getType().name() + ":" + groupId;
            stats.merge(
                    key,
                    new CategoryStat(
                            groupId,
                            groupName,
                            groupColor,
                            groupIcon,
                            t.getType(),
                            t.getAmount(),
                            null,
                            null),
                    (existing, added) -> new CategoryStat(
                            existing.categoryId(),
                            existing.categoryName(),
                            existing.color(),
                            existing.icon(),
                            existing.type(),
                            existing.amount().add(added.amount()),
                            null,
                            null));
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

    private static final Long DELETED_USER_KEY = 0L;

    private List<MemberStat> byMember(List<Transaction> transactions) {
        Map<Long, MemberStat> stats = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            var user = t.getUser();
            Long key = user != null ? user.getId() : DELETED_USER_KEY;
            Long userId = user != null ? user.getId() : null;
            String userName = user != null ? user.getName() : "탈퇴한 사용자";
            BigDecimal income = t.getType() == TransactionType.INCOME ? t.getAmount() : BigDecimal.ZERO;
            BigDecimal expense = t.getType() == TransactionType.EXPENSE ? t.getAmount() : BigDecimal.ZERO;
            stats.merge(key, new MemberStat(userId, userName, income, expense),
                    (existing, added) -> new MemberStat(existing.userId(), existing.userName(),
                            existing.income().add(added.income()), existing.expense().add(added.expense())));
        }
        return List.copyOf(stats.values());
    }
}
