package com.bosu.housebook.recurringexpense;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.transaction.Transaction;
import com.bosu.housebook.transaction.TransactionRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 매일 새벽, 오늘이 시행일인 활성 고정비 지출을 찾아 거래로 자동 생성한다. 같은 달에 이미
 * 생성했으면(lastGeneratedDate 기준) 서버 재시작 등으로 배치가 다시 돌아도 중복 생성하지 않는다. */
@Service
public class RecurringExpenseGenerationService {

    private static final Logger log = LoggerFactory.getLogger(RecurringExpenseGenerationService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final TransactionRepository transactionRepository;

    public RecurringExpenseGenerationService(RecurringExpenseRepository recurringExpenseRepository,
            TransactionRepository transactionRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void generateDueTransactions() {
        generateDueTransactions(LocalDate.now(KST));
    }

    @Transactional
    public void generateDueTransactions(LocalDate today) {
        List<RecurringExpense> active = recurringExpenseRepository.findByActiveTrue();
        for (RecurringExpense recurringExpense : active) {
            try {
                generateIfDue(recurringExpense, today);
            } catch (Exception e) {
                log.warn("고정비 자동 생성 실패: recurringExpenseId={}, date={}", recurringExpense.getId(), today, e);
            }
        }
    }

    private void generateIfDue(RecurringExpense recurringExpense, LocalDate today) {
        int effectiveDay = Math.min(recurringExpense.getDayOfMonth(), today.lengthOfMonth());
        if (today.getDayOfMonth() != effectiveDay) {
            return;
        }
        LocalDate last = recurringExpense.getLastGeneratedDate();
        if (last != null && last.getYear() == today.getYear() && last.getMonthValue() == today.getMonthValue()) {
            return;
        }

        Transaction transaction = new Transaction(
                recurringExpense.getHousehold(),
                TransactionType.EXPENSE,
                recurringExpense.getAmount(),
                today,
                recurringExpense.getCategory(),
                null,
                recurringExpense.getCreatedBy(),
                buildMemo(recurringExpense));
        transactionRepository.save(transaction);
        recurringExpense.markGenerated(today);
    }

    private String buildMemo(RecurringExpense recurringExpense) {
        String memo = recurringExpense.getMemo();
        if (memo == null || memo.isBlank()) {
            return recurringExpense.getName();
        }
        return recurringExpense.getName() + " · " + memo;
    }
}
