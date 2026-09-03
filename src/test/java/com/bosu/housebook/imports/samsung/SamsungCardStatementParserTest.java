package com.bosu.housebook.imports.samsung;

import static org.assertj.core.api.Assertions.assertThat;

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

class SamsungCardStatementParserTest {

    private final SamsungCardStatementParser parser = new SamsungCardStatementParser();

    /** "일시불+할부_카드이용내역조회" 다운로드 포맷 — 승인 단위 원시 내역, 전체취소 건은 상쇄해서 뺀다. */
    @Test
    void parsesUsageDetailExcelAndNetsOutCancelledApprovals() throws Exception {
        byte[] bytes = buildUsageDetailExcel();

        List<ParsedTransaction> result = parser.parse(new ByteArrayInputStream(bytes));

        // 41707204 승인건은 취소행(-61380)과 상쇄되어 빠지고, 정상 결제 2건만 남는다.
        assertThat(result).hasSize(2);

        ParsedTransaction first = result.get(0);
        assertThat(first.transactionDate()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(first.merchantName()).isEqualTo("토스페이_테무");
        assertThat(first.amount()).isEqualByComparingTo(new BigDecimal("32802"));

        ParsedTransaction second = result.get(1);
        assertThat(second.transactionDate()).isEqualTo(LocalDate.of(2026, 8, 25));
        assertThat(second.merchantName()).isEqualTo("주식회사더즌");
        assertThat(second.amount()).isEqualByComparingTo(new BigDecimal("1330"));
    }

    private byte[] buildUsageDetailExcel() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet summary = workbook.createSheet("■ 카드이용내역");
            writeStringRow(summary, 0, "조회기간", "이용지역", "이용구분", "금액합계(원)", "건수합계(건)");
            writeStringRow(summary, 1, "2026.08.01 ~ 2026.08.31", "국내", "일시불+할부", "34132", "3");

            Sheet detail = workbook.createSheet("■ 국내이용내역");
            writeStringRow(detail, 0, "카드번호", "본인가족구분", "승인일자", "승인시각", "가맹점명", "승인금액(원)",
                    "일시불할부구분", "할부개월", "승인번호", "취소여부", "사용포인트", "결제일");
            writeDetailRow(detail, 1, "379183******248", "2026.08.31", "15:32:50", "토스페이_테무", 32802,
                    "50219761", "-", " ");
            writeDetailRow(detail, 2, "379183******248", "2026.08.25", "11:05:30", "주식회사더즌", 1330,
                    "42410011", "-", "20260913");
            writeDetailRow(detail, 3, "379183******248", "2026.08.24", "16:27:50", "주식회사 버킷플레이스", 61380,
                    "41707204", "-", " ");
            writeDetailRow(detail, 4, "379183******248", "2026.08.24", "16:27:50", "주식회사 버킷플레이스", -61380,
                    "41707204", "전체취소", " ");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** 실제 다운로드 파일과 같은 셀 타입으로 맞춘다: 승인금액(원)만 숫자 셀, 나머지는 전부 문자열 셀. */
    private void writeDetailRow(Sheet sheet, int rowIndex, String cardNumber, String approvalDate, String approvalTime,
            String merchant, double amount, String approvalNo, String cancelStatus, String paymentDate) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(cardNumber);
        row.createCell(1).setCellValue("본인");
        row.createCell(2).setCellValue(approvalDate);
        row.createCell(3).setCellValue(approvalTime);
        row.createCell(4).setCellValue(merchant);
        row.createCell(5).setCellValue(amount);
        row.createCell(6).setCellValue("일시불");
        row.createCell(7).setCellValue("0");
        row.createCell(8).setCellValue(approvalNo);
        row.createCell(9).setCellValue(cancelStatus);
        row.createCell(10).setCellValue("0");
        row.createCell(11).setCellValue(paymentDate);
    }

    private void writeStringRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}
