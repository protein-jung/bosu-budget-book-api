package com.bosu.housebook.imports;

import com.bosu.housebook.common.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedTransaction(LocalDate transactionDate, String merchantName, BigDecimal amount,
        TransactionType type, boolean looseDuplicateCheck) {

    /** 카드/주문 명세서 파서는 전부 지출만 다루니 기존 호출부는 이 생성자로 그대로 쓸 수 있다. */
    public ParsedTransaction(LocalDate transactionDate, String merchantName, BigDecimal amount) {
        this(transactionDate, merchantName, amount, TransactionType.EXPENSE, false);
    }

    /**
     * looseDuplicateCheck가 true면 중복 판정 시 메모(가맹점명/적요내용)는 안 보고 날짜·금액·유형만
     * 본다. 케이뱅크 계좌 거래내역의 "체크결제" 행처럼, 같은 지출이 다른 명세서(PDF 이용대금명세서)엔
     * 표기가 다른 가맹점명으로 이미 들어있을 수 있는 경우에 쓴다.
     */
    public ParsedTransaction(LocalDate transactionDate, String merchantName, BigDecimal amount, TransactionType type) {
        this(transactionDate, merchantName, amount, type, false);
    }
}
