package com.bosu.housebook.asset;

/** LOAN 자산의 상환 방식. 원리금균등은 매달 납입금(원금+이자)이 고정, 원금균등은 매달 원금
 * 상환액이 고정이고 이자가 잔액에 비례해 줄어들어 총 납입금이 매달 감소한다. */
public enum LoanRepaymentType {
    EQUAL_INSTALLMENT,
    EQUAL_PRINCIPAL
}
