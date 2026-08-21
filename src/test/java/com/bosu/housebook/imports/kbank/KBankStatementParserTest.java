package com.bosu.housebook.imports.kbank;

import static org.assertj.core.api.Assertions.assertThat;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.imports.ParsedTransaction;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class KBankStatementParserTest {

    private final KBankStatementParser parser = new KBankStatementParser();

    @Test
    void parsesTransferExcelIntoIncomeAndExpense() throws Exception {
        byte[] bytes = buildTransferExcel();

        List<ParsedTransaction> result = parser.parse(new ByteArrayInputStream(bytes));

        // "모두 다 캐시백"은 잡음이라 아예 빠진다. 체크결제는 그대로 들어오되(단독 업로드 시 데이터
        // 유실 방지) 메모가 PDF 이용대금명세서와 다르게 찍힐 수 있어 looseDuplicateCheck만 켜진다.
        assertThat(result).hasSize(2);

        ParsedTransaction checkPayment = result.get(0);
        assertThat(checkPayment.transactionDate()).isEqualTo(LocalDate.of(2026, 8, 20));
        assertThat(checkPayment.merchantName()).isEqualTo("오포농업협동조합");
        assertThat(checkPayment.amount()).isEqualByComparingTo(new BigDecimal("23780"));
        assertThat(checkPayment.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(checkPayment.looseDuplicateCheck()).isTrue();

        ParsedTransaction autoTransfer = result.get(1);
        assertThat(autoTransfer.transactionDate()).isEqualTo(LocalDate.of(2026, 8, 18));
        assertThat(autoTransfer.merchantName()).isEqualTo("정기 적금 자동이체");
        assertThat(autoTransfer.amount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(autoTransfer.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(autoTransfer.looseDuplicateCheck()).isFalse();
    }

    private byte[] buildTransferExcel() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("거래내역");

            writeRow(sheet, 0, "고객명", "박보영", "계좌번호", "100-100-348351", "저장일시", "2026.08.20 13:33");
            writeRow(sheet, 2, "거래일시", "거래구분", "입금금액", "출금금액", "잔액", "상대 예금주명", "상대 은행",
                    "상대 계좌번호", "적요내용", "메모");
            writeRow(sheet, 3, "2026.08.20 11:01:27", "캐시백", "118", "0", "243,461", "모두 다 캐시백", "", "",
                    "모두 다 캐시백", "");
            writeRow(sheet, 4, "2026.08.20 11:01:14", "체크결제", "0", "23,780", "243,343", "", "", "",
                    "오포농업협동조합", "");
            writeRow(sheet, 5, "2026.08.18 09:00:00", "자동이체", "0", "100,000", "219,563", "케이뱅크", "", "",
                    "정기 적금 자동이체", "");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}
