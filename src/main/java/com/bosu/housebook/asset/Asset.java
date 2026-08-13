package com.bosu.housebook.asset;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(nullable = false)
    private String name;

    /** 은행/증권사/거래소 등 보관처. 자유 입력. */
    private String custodian;

    /** STOCK/CRYPTO 전용: 티커(005930.KS, AAPL) 또는 코인 심볼(BTC). */
    private String symbol;

    /** STOCK/CRYPTO 전용: 보유 수량. */
    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    /** STOCK/CRYPTO 전용(선택): 매수 평단가(원). */
    @Column(name = "average_price", precision = 20, scale = 4)
    private BigDecimal averagePrice;

    /** 그 외 자산 전용: 직접 입력한 평가금액(원). */
    @Column(name = "manual_value", precision = 16, scale = 2)
    private BigDecimal manualValue;

    /** STOCK/CRYPTO 전용: 마지막으로 조회된 원화 단가. */
    @Column(name = "current_price", precision = 20, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;

    private String memo;

    /** 부동산 전용(선택): 주소/동/호수. 다른 타입도 막지는 않는다 — 그냥 안 쓸 뿐. */
    private String address;

    private String dong;

    private String ho;

    /** 부동산 전용(선택): 국토부 실거래가 자동 조회에 쓰는 법정동 코드/단지명/법정동명. */
    @Column(name = "lawd_cd")
    private String lawdCd;

    @Column(name = "complex_name")
    private String complexName;

    @Column(name = "region_dong_name")
    private String regionDongName;

    /** STOCK 전용(선택): 일반 계좌 보유인지 연금(IRP/연금저축) 계좌 보유인지. 기본값 GENERAL. */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_category", nullable = false)
    private AccountCategory accountCategory = AccountCategory.GENERAL;

    /** 부동산/차량 외 자산 전용(선택): 소유주(가계부 구성원). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    /** CASH 전용: 계좌(수시입출금)/예금/적금 구분. 기본값 ACCOUNT. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cash_category")
    private CashCategory cashCategory;

    /** CASH 전용(선택, 예금/적금만): 만기일. 지나면 만기로 취급한다. */
    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    /** CASH 전용(선택, 예금/적금만): 연이율(%, 예: 3.5). 단리로 이자를 추정하는 데 쓰인다. */
    @Column(name = "cash_interest_rate", precision = 6, scale = 3)
    private BigDecimal cashInterestRate;

    /** CASH 전용(선택, 예금/적금만): 예치 시작일. */
    @Column(name = "cash_start_date")
    private LocalDate cashStartDate;

    /** 차량 전용(선택): 구매일. 표시용으로만 쓰인다 — 현재가는 encarUrl 기반 시세 조회로 구한다. */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /** 차량 전용(선택): 같은 차종의 매물 목록을 보여주는 encar.com URL. 새로고침 시 이 URL이 가리키는
     * 매물들의 평균 가격을 조회해 currentPrice에 캐시한다 — 없으면 manualValue(구매가)를 현재가로 취급한다. */
    @Column(name = "encar_url", length = 2000)
    private String encarUrl;

    /** 대출 전용: 원금. */
    @Column(name = "loan_principal", precision = 16, scale = 2)
    private BigDecimal loanPrincipal;

    /** 대출 전용: 대출 시작년월(그 달 아무 날짜로 저장돼도 월 단위로만 계산에 쓰인다). */
    @Column(name = "loan_start_month")
    private LocalDate loanStartMonth;

    /** 대출 전용: 상환 기한(개월 수). */
    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    /** 대출 전용: 월 납입금(원리금). */
    @Column(name = "loan_monthly_payment", precision = 16, scale = 2)
    private BigDecimal loanMonthlyPayment;

    /** 대출 전용: 연이율(%, 예: 4.5). */
    @Column(name = "loan_interest_rate", precision = 6, scale = 3)
    private BigDecimal loanInterestRate;

    /** 대출 전용: 상환 방식(원리금균등/원금균등). 기본값 EQUAL_INSTALLMENT. */
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_repayment_type")
    private LoanRepaymentType loanRepaymentType;

    public Asset(Household household, AssetType type, String name, String custodian, String symbol,
            BigDecimal quantity, BigDecimal averagePrice, BigDecimal manualValue, String memo, String address,
            String dong, String ho, String lawdCd, String complexName, String regionDongName,
            AccountCategory accountCategory, User owner, CashCategory cashCategory, LocalDate maturityDate,
            BigDecimal cashInterestRate, LocalDate cashStartDate, LocalDate purchaseDate, String encarUrl,
            BigDecimal loanPrincipal, LocalDate loanStartMonth, Integer loanTermMonths,
            BigDecimal loanMonthlyPayment, BigDecimal loanInterestRate, LoanRepaymentType loanRepaymentType) {
        this.household = household;
        this.type = type;
        this.name = name;
        this.custodian = custodian;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.manualValue = manualValue;
        this.memo = memo;
        this.address = address;
        this.dong = dong;
        this.ho = ho;
        this.lawdCd = lawdCd;
        this.complexName = complexName;
        this.regionDongName = regionDongName;
        this.accountCategory = accountCategory != null ? accountCategory : AccountCategory.GENERAL;
        this.owner = owner;
        this.cashCategory = type == AssetType.CASH ? (cashCategory != null ? cashCategory : CashCategory.ACCOUNT) : null;
        this.maturityDate = type == AssetType.CASH ? maturityDate : null;
        this.cashInterestRate = type == AssetType.CASH ? cashInterestRate : null;
        this.cashStartDate = type == AssetType.CASH ? cashStartDate : null;
        this.purchaseDate = type == AssetType.VEHICLE ? purchaseDate : null;
        this.encarUrl = type == AssetType.VEHICLE ? encarUrl : null;
        boolean isLoan = type == AssetType.LOAN;
        this.loanPrincipal = isLoan ? loanPrincipal : null;
        this.loanStartMonth = isLoan ? loanStartMonth : null;
        this.loanTermMonths = isLoan ? loanTermMonths : null;
        this.loanMonthlyPayment = isLoan ? loanMonthlyPayment : null;
        this.loanInterestRate = isLoan ? loanInterestRate : null;
        this.loanRepaymentType = isLoan
                ? (loanRepaymentType != null ? loanRepaymentType : LoanRepaymentType.EQUAL_INSTALLMENT)
                : null;
    }

    public void update(AssetType type, String name, String custodian, String symbol, BigDecimal quantity,
            BigDecimal averagePrice, BigDecimal manualValue, String memo, String address, String dong, String ho,
            String lawdCd, String complexName, String regionDongName, AccountCategory accountCategory,
            CashCategory cashCategory, LocalDate maturityDate, BigDecimal cashInterestRate, LocalDate cashStartDate,
            LocalDate purchaseDate, String encarUrl, BigDecimal loanPrincipal, LocalDate loanStartMonth,
            Integer loanTermMonths, BigDecimal loanMonthlyPayment, BigDecimal loanInterestRate,
            LoanRepaymentType loanRepaymentType) {
        this.type = type;
        this.name = name;
        this.custodian = custodian;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.manualValue = manualValue;
        this.memo = memo;
        this.address = address;
        this.dong = dong;
        this.ho = ho;
        this.lawdCd = lawdCd;
        this.complexName = complexName;
        this.regionDongName = regionDongName;
        this.accountCategory = accountCategory != null ? accountCategory : AccountCategory.GENERAL;
        this.cashCategory = type == AssetType.CASH ? (cashCategory != null ? cashCategory : CashCategory.ACCOUNT) : null;
        this.maturityDate = type == AssetType.CASH ? maturityDate : null;
        this.cashInterestRate = type == AssetType.CASH ? cashInterestRate : null;
        this.cashStartDate = type == AssetType.CASH ? cashStartDate : null;
        this.purchaseDate = type == AssetType.VEHICLE ? purchaseDate : null;
        this.encarUrl = type == AssetType.VEHICLE ? encarUrl : null;
        boolean isLoan = type == AssetType.LOAN;
        this.loanPrincipal = isLoan ? loanPrincipal : null;
        this.loanStartMonth = isLoan ? loanStartMonth : null;
        this.loanTermMonths = isLoan ? loanTermMonths : null;
        this.loanMonthlyPayment = isLoan ? loanMonthlyPayment : null;
        this.loanInterestRate = isLoan ? loanInterestRate : null;
        this.loanRepaymentType = isLoan
                ? (loanRepaymentType != null ? loanRepaymentType : LoanRepaymentType.EQUAL_INSTALLMENT)
                : null;
    }

    public void updatePrice(BigDecimal price, LocalDateTime updatedAt) {
        this.currentPrice = price;
        this.priceUpdatedAt = updatedAt;
    }

    /** REAL_ESTATE는 국토부 실거래가(있으면)를 우선하고 없으면 직접 입력값, VEHICLE은 encarUrl 기반으로
     * 조회해 캐시해둔 매물 평균가(있으면)를 우선하고 없으면 직접 입력한 구매가, LOAN은 상환 스케줄로
     * 추정한 잔액을 음수로, 심볼이 있는 STOCK/CRYPTO는 단가*수량, 그 외(심볼 없는 STOCK/CRYPTO
     * 포함 — 예: 계좌 내 현금성 잔고, 원리금보장형 상품)는 직접 입력한 평가금액. 시세를 아직 못
     * 받아왔으면 null. */
    public BigDecimal getCurrentValue() {
        if (type == AssetType.REAL_ESTATE || type == AssetType.VEHICLE) {
            return currentPrice != null ? currentPrice : manualValue;
        }
        if (type == AssetType.LOAN) {
            BigDecimal remaining = estimateLoanRemainingBalance();
            return remaining != null ? remaining.negate() : null;
        }
        if (type.isLivePriced() && symbol != null && !symbol.isBlank()) {
            return currentPrice != null && quantity != null ? currentPrice.multiply(quantity) : null;
        }
        if (type == AssetType.CASH) {
            BigDecimal accrued = estimateCashAccruedValue();
            return accrued != null ? accrued : manualValue;
        }
        return manualValue;
    }

    /** 예금/적금 단리 이자 추정: 원금 * 연이율 * 경과일수/365. 만기가 지났으면 만기일까지만 계산하고
     * 더 늘리지 않는다. 원금/이율/시작일 중 하나라도 없으면 null(호출부에서 manualValue로 대체). */
    private BigDecimal estimateCashAccruedValue() {
        if (manualValue == null || cashInterestRate == null || cashStartDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (!today.isAfter(cashStartDate)) {
            return manualValue;
        }
        long elapsedDays = ChronoUnit.DAYS.between(cashStartDate, today);
        if (maturityDate != null) {
            long totalDays = Math.max(ChronoUnit.DAYS.between(cashStartDate, maturityDate), 0);
            elapsedDays = Math.min(elapsedDays, totalDays);
        }
        double interest = manualValue.doubleValue() * (cashInterestRate.doubleValue() / 100.0) * elapsedDays / 365.0;
        return manualValue.add(BigDecimal.valueOf(interest)).setScale(0, RoundingMode.HALF_UP);
    }

    /** 상환 방식에 따라 경과 개월 기준 잔액을 추정한다. 필수 입력값이 없으면 null. */
    private BigDecimal estimateLoanRemainingBalance() {
        if (loanPrincipal == null || loanStartMonth == null || loanTermMonths == null || loanTermMonths <= 0) {
            return null;
        }
        long elapsedMonths = elapsedLoanMonths();
        if (elapsedMonths == 0) {
            return loanPrincipal;
        }
        if (elapsedMonths >= loanTermMonths) {
            return BigDecimal.ZERO;
        }
        if (loanRepaymentType == LoanRepaymentType.EQUAL_PRINCIPAL) {
            return estimateEqualPrincipalBalance(elapsedMonths);
        }
        return estimateEqualInstallmentBalance(elapsedMonths);
    }

    /** 대출 시작월부터 오늘까지 경과한 개월 수(0~상환기한 범위로 clamp). */
    private long elapsedLoanMonths() {
        long elapsedMonths = ChronoUnit.MONTHS.between(loanStartMonth.withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1));
        return Math.max(0, Math.min(elapsedMonths, loanTermMonths));
    }

    /** 이번 달 기준 실제 납입액. 원리금균등은 항상 고정 월납입금 그대로, 원금균등은 원금 상환분
     * (고정) + 이번 달 시점 잔액에 대한 이자로 계산해 매달 조금씩 줄어드는 값을 보여준다. 대출이
     * 이미 완제됐거나 필수 입력값이 없으면 null. */
    public BigDecimal getCurrentMonthlyPayment() {
        if (type != AssetType.LOAN || loanPrincipal == null || loanStartMonth == null || loanTermMonths == null
                || loanTermMonths <= 0 || loanInterestRate == null) {
            return null;
        }
        long elapsedMonths = elapsedLoanMonths();
        if (elapsedMonths >= loanTermMonths) {
            return BigDecimal.ZERO;
        }
        if (loanRepaymentType != LoanRepaymentType.EQUAL_PRINCIPAL) {
            return loanMonthlyPayment;
        }
        BigDecimal monthlyPrincipal = loanPrincipal.divide(BigDecimal.valueOf(loanTermMonths), 10,
                RoundingMode.HALF_UP);
        BigDecimal balanceBeforeThisMonth = estimateEqualPrincipalBalance(elapsedMonths);
        BigDecimal monthlyRate = loanInterestRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal interest = balanceBeforeThisMonth.multiply(monthlyRate);
        return monthlyPrincipal.add(interest).setScale(0, RoundingMode.HALF_UP);
    }

    /** 원금균등상환: 매달 원금을 균등하게(원금/기간) 갚으므로 잔액은 경과 개월에 비례해 선형으로
     * 줄어든다 — 이자율/월납입금과 무관하다. */
    private BigDecimal estimateEqualPrincipalBalance(long elapsedMonths) {
        BigDecimal monthlyPrincipal = loanPrincipal.divide(BigDecimal.valueOf(loanTermMonths), 10,
                RoundingMode.HALF_UP);
        BigDecimal paidPrincipal = monthlyPrincipal.multiply(BigDecimal.valueOf(elapsedMonths));
        BigDecimal remaining = loanPrincipal.subtract(paidPrincipal).max(BigDecimal.ZERO);
        return remaining.setScale(0, RoundingMode.HALF_UP);
    }

    /** 원리금균등상환: 경과 개월만큼 매달 이자(잔액*연이율/12)를 떼고 나머지를 원금 상환에 쓰는
     * 방식으로 시뮬레이션해 잔액을 추정한다. */
    private BigDecimal estimateEqualInstallmentBalance(long elapsedMonths) {
        if (loanMonthlyPayment == null || loanInterestRate == null) {
            return null;
        }
        double monthlyRate = loanInterestRate.doubleValue() / 100.0 / 12.0;
        double balance = loanPrincipal.doubleValue();
        double payment = loanMonthlyPayment.doubleValue();
        for (long i = 0; i < elapsedMonths && balance > 0; i++) {
            double interest = balance * monthlyRate;
            balance -= payment - interest;
        }
        balance = Math.max(balance, 0);
        return BigDecimal.valueOf(balance).setScale(0, RoundingMode.HALF_UP);
    }

    /** 예금/적금 만기일이 지났는지. 만기일이 없으면 false. */
    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }
}
