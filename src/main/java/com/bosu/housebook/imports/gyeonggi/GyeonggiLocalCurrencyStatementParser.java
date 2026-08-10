package com.bosu.housebook.imports.gyeonggi;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.imports.ExcelParsingUtils;
import com.bosu.housebook.imports.ImportProvider;
import com.bosu.housebook.imports.ParsedTransaction;
import com.bosu.housebook.imports.StatementParser;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * 경기지역화폐(코나아이) "거래내역서" xlsx: 거래일시/가맹점명/총 결제금액 등 헤더 행을
 * 동적으로 찾아 그 아래 거래 행을 날짜 패턴이 끊길 때(월별 소계/총 금액 행)까지 읽는다.
 * 할인·쿠폰이 반영된 실제 결제금액인 "총 결제금액"을 사용한다.
 */
@Component
public class GyeonggiLocalCurrencyStatementParser implements StatementParser {

    private static final Pattern DATETIME_PATTERN = Pattern.compile("\\d{4}/\\d{2}/\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final String HEADER_DATE = "거래일시";
    private static final String HEADER_MERCHANT = "가맹점명";
    private static final String HEADER_AMOUNT = "총 결제금액";

    @Override
    public ImportProvider provider() {
        return ImportProvider.GYEONGGI_LOCAL_CURRENCY;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            ExcelParsingUtils.HeaderRow header = ExcelParsingUtils.findHeaderRow(sheet,
                    Set.of(HEADER_DATE, HEADER_MERCHANT, HEADER_AMOUNT));
            int dateCol = header.column(HEADER_DATE);
            int merchantCol = header.column(HEADER_MERCHANT);
            int amountCol = header.column(HEADER_AMOUNT);

            List<ParsedTransaction> transactions = new ArrayList<>();
            for (int r = header.rowIndex() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String dateText = ExcelParsingUtils.stringValue(row.getCell(dateCol)).trim();
                if (dateText.isEmpty()) {
                    continue;
                }
                if (!DATETIME_PATTERN.matcher(dateText).matches()) {
                    break;
                }

                String merchant = ExcelParsingUtils.stringValue(row.getCell(merchantCol)).trim();
                BigDecimal amount = ExcelParsingUtils.numericValue(row.getCell(amountCol));
                if (merchant.isEmpty() || amount == null || amount.signum() <= 0) {
                    continue;
                }

                LocalDate date = java.time.LocalDateTime.parse(dateText, DATETIME_FORMAT).toLocalDate();
                transactions.add(new ParsedTransaction(date, merchant, amount));
            }
            return transactions;
        } catch (IOException e) {
            throw ApiException.badRequest("엑셀 파일을 읽을 수 없습니다.");
        }
    }
}
