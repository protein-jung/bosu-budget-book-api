package com.bosu.housebook.admin;

import com.bosu.housebook.admin.dto.AdminCategoryTotalResponse;
import com.bosu.housebook.admin.dto.AdminHouseholdDetailResponse;
import com.bosu.housebook.admin.dto.AdminHouseholdResponse;
import com.bosu.housebook.admin.dto.AdminLoginRequest;
import com.bosu.housebook.admin.dto.AdminMemberResponse;
import com.bosu.housebook.admin.dto.AdminStatsResponse;
import com.bosu.housebook.admin.dto.AdminTransactionResponse;
import com.bosu.housebook.admin.dto.AdminTrendPointResponse;
import com.bosu.housebook.admin.dto.AdminTrendsResponse;
import com.bosu.housebook.admin.dto.AdminUserDetailResponse;
import com.bosu.housebook.admin.dto.AdminUserResponse;
import com.bosu.housebook.auth.JwtTokenProvider;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.config.AdminProperties;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.household.HouseholdMember;
import com.bosu.housebook.household.HouseholdMemberRepository;
import com.bosu.housebook.household.HouseholdRepository;
import com.bosu.housebook.transaction.Transaction;
import com.bosu.housebook.transaction.TransactionRepository;
import com.bosu.housebook.transaction.TransactionService;
import com.bosu.housebook.transaction.dto.TransactionResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AdminService(AdminProperties adminProperties, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
            HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository,
            TransactionRepository transactionRepository, TransactionService transactionService) {
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    public String login(AdminLoginRequest request) {
        boolean usernameMatches = adminProperties.username().equals(request.username());
        boolean passwordMatches = !adminProperties.passwordHash().isBlank()
                && passwordEncoder.matches(request.password(), adminProperties.passwordHash());
        if (!usernameMatches || !passwordMatches) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return jwtTokenProvider.generateAdminToken();
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public List<AdminHouseholdResponse> listHouseholds() {
        return householdRepository.findAll().stream()
                .map(this::toAdminHouseholdResponse)
                .toList();
    }

    public AdminHouseholdDetailResponse householdDetail(Long householdId) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> ApiException.notFound("가계부를 찾을 수 없습니다."));

        List<AdminMemberResponse> members = householdMemberRepository.findByHouseholdId(householdId).stream()
                .map(m -> new AdminMemberResponse(m.getUser().getId(), m.getUser().getName(), m.getUser().getEmail(),
                        m.getRole().name(), m.getJoinedAt()))
                .toList();

        List<AdminCategoryTotalResponse> categoryTotals = transactionRepository
                .findCategoryTotalsByHouseholdId(householdId).stream()
                .map(p -> new AdminCategoryTotalResponse(p.getCategoryName(), p.getCategoryIcon(),
                        p.getCategoryColor(), p.getType().name(), p.getTotal(), p.getCount()))
                .toList();

        return new AdminHouseholdDetailResponse(
                household.getId(),
                household.getName(),
                household.getInviteCode(),
                household.getCreatedAt(),
                members,
                categoryTotals,
                transactionRepository.countByHouseholdId(householdId));
    }

    /** 관리자 화면에서 가계부 하나를 실제 앱의 달력 화면과 똑같은 모양으로 보여줄 때 쓴다 —
     * 응답 형태가 /api/transactions와 동일해서 프론트가 그 화면 컴포넌트를 그대로 재사용한다. */
    public List<TransactionResponse> householdMonthlyTransactions(Long householdId, int year, int month) {
        return transactionService.getMonthlyByHousehold(householdId, year, month);
    }

    public AdminUserDetailResponse userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        var membership = householdMemberRepository.findByUserId(userId).orElse(null);
        Household household = membership != null ? membership.getHousehold() : null;

        List<AdminTransactionResponse> recentTransactions = transactionRepository
                .findTop50ByUserIdOrderByTransactionDateDescIdDesc(userId).stream()
                .map(this::toAdminTransactionResponse)
                .toList();

        return new AdminUserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBirthDate(),
                user.isBlocked(),
                user.getCreatedAt(),
                household != null ? household.getId() : null,
                household != null ? household.getName() : null,
                membership != null ? membership.getRole().name() : null,
                transactionRepository.countByUserId(userId),
                recentTransactions);
    }

    public AdminStatsResponse stats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Map<TransactionType, BigDecimal> totalsByType = transactionRepository.findTotalAmountsByType().stream()
                .collect(Collectors.toMap(TransactionRepository.TypeTotalProjection::getType,
                        TransactionRepository.TypeTotalProjection::getTotal));
        return new AdminStatsResponse(
                userRepository.count(),
                householdRepository.count(),
                transactionRepository.count(),
                totalsByType.getOrDefault(TransactionType.INCOME, BigDecimal.ZERO),
                totalsByType.getOrDefault(TransactionType.EXPENSE, BigDecimal.ZERO),
                userRepository.countByCreatedAtAfter(sevenDaysAgo),
                householdRepository.countByCreatedAtAfter(sevenDaysAgo));
    }

    /** 관리자 대시보드의 회원 증가/일별 수입·지출 추이 그래프. */
    public AdminTrendsResponse trends(int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);

        List<AdminTrendPointResponse> userGrowth = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            long cumulativeUsers = userRepository.countByCreatedAtLessThanEqual(date.atTime(LocalTime.MAX));
            userGrowth.add(new AdminTrendPointResponse(date.toString(), BigDecimal.valueOf(cumulativeUsers)));
        }

        Map<LocalDate, BigDecimal> incomeByDate = new HashMap<>();
        Map<LocalDate, BigDecimal> expenseByDate = new HashMap<>();
        for (var row : transactionRepository.findDailyTotalsByTypeBetween(start, today)) {
            if (row.getType() == TransactionType.INCOME) {
                incomeByDate.put(row.getTransactionDate(), row.getTotal());
            } else {
                expenseByDate.put(row.getTransactionDate(), row.getTotal());
            }
        }

        List<AdminTrendPointResponse> dailyIncome = new ArrayList<>();
        List<AdminTrendPointResponse> dailyExpense = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            dailyIncome.add(new AdminTrendPointResponse(date.toString(), incomeByDate.getOrDefault(date, BigDecimal.ZERO)));
            dailyExpense.add(
                    new AdminTrendPointResponse(date.toString(), expenseByDate.getOrDefault(date, BigDecimal.ZERO)));
        }

        return new AdminTrendsResponse(userGrowth, dailyIncome, dailyExpense);
    }

    @Transactional
    public void setBlocked(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        user.updateBlocked(blocked);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        var membership = householdMemberRepository.findByUserId(user.getId()).orElse(null);
        Household household = membership != null ? membership.getHousehold() : null;
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBirthDate(),
                user.isBlocked(),
                user.getCreatedAt(),
                household != null ? household.getId() : null,
                household != null ? household.getName() : null,
                membership != null ? membership.getRole().name() : null,
                transactionRepository.countByUserId(user.getId()));
    }

    private AdminTransactionResponse toAdminTransactionResponse(Transaction transaction) {
        return new AdminTransactionResponse(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getCategory().getName(),
                transaction.getCategory().getIcon(),
                transaction.getMemo(),
                transaction.getUser() != null ? transaction.getUser().getName() : null,
                transaction.getCard() != null ? transaction.getCard().getName() : null);
    }

    private AdminHouseholdResponse toAdminHouseholdResponse(Household household) {
        List<String> memberNames = householdMemberRepository.findByHouseholdId(household.getId()).stream()
                .map(HouseholdMember::getUser)
                .map(User::getName)
                .toList();
        return new AdminHouseholdResponse(
                household.getId(),
                household.getName(),
                household.getInviteCode(),
                household.getCreatedAt(),
                memberNames,
                transactionRepository.countByHouseholdId(household.getId()));
    }
}
