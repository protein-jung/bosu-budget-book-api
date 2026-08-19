package com.bosu.housebook.statistics;

import com.bosu.housebook.category.Category;
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
    private final HouseholdService householdService;

    public StatisticsService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
            HouseholdService householdService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.householdService = householdService;
    }

    public MonthlySummaryResponse getMonthlySummary(Long userId, int year, int month) {
        Long householdId = householdService.getHouseholdIdForUser(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Transaction> transactions = loadTransactions(householdId, yearMonth, yearMonth);
        List<Category> categories = categoryRepository.findByHouseholdIdOrderBySortOrderAscIdAsc(householdId);
        return buildMonthlySummary(categories, transactions);
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
                    MonthlySummaryResponse summary = buildMonthlySummary(categories, entry.getValue());
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

    private MonthlySummaryResponse buildMonthlySummary(List<Category> categories, List<Transaction> transactions) {
        // 미래준비(저축/투자) 카테고리는 지출이 아니므로 지출 합계·그룹별/카드별/사람별 통계에서 제외한다.
        // 다만 저축 목표 대비 진행률(buildBudgets)은 byCategory를 그대로 써서 정상 동작해야 하므로
        // byCategory 자체는 필터링하지 않는다.
        List<Transaction> expenseStatsTransactions = transactions.stream()
                .filter(t -> t.getType() != TransactionType.EXPENSE || !t.getCategory().isExcludedFromExpenseStats())
                .toList();
        BigDecimal totalIncome = sumByType(expenseStatsTransactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(expenseStatsTransactions, TransactionType.EXPENSE);
        List<CategoryStat> byCategoryList = byCategory(transactions);
        return new MonthlySummaryResponse(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                byCategoryList,
                byParentCategory(expenseStatsTransactions),
                byCard(expenseStatsTransactions),
                byMember(expenseStatsTransactions),
                buildBudgets(categories, byCategoryList));
    }

    /** 예산 목표는 달마다 따로 잡지 않고 카테고리에 한 번 설정해두면 매달 그대로 적용된다. */
    private List<CategoryBudget> buildBudgets(List<Category> categories, List<CategoryStat> byCategoryList) {
        Map<Long, BigDecimal> spentByCategory = byCategoryList.stream()
                .collect(Collectors.toMap(CategoryStat::categoryId, CategoryStat::amount));

        List<CategoryBudget> budgets = new ArrayList<>();
        for (Category category : categories) {
            BigDecimal target = category.getTargetAmount();
            if (target == null) {
                continue;
            }
            BigDecimal spent = spentByCategory.getOrDefault(category.getId(), BigDecimal.ZERO);
            budgets.add(new CategoryBudget(category.getId(), category.getName(), category.getColor(),
                    category.getIcon(), category.getType(), target, spent));
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
