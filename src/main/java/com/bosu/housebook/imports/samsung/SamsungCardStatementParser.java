package com.bosu.housebook.imports.samsung;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.imports.ExcelParsingUtils;
import com.bosu.housebook.imports.ImportProvider;
import com.bosu.housebook.imports.ParsedTransaction;
import com.bosu.housebook.imports.StatementParser;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * 삼성카드 명세서 파서. 두 가지 내보내기 포맷을 모두 지원한다.
 *
 * <ul>
 *   <li>신규 포맷(2026.08~): "일시불"/"해외이용"/"청구요약" 시트로 분리, 날짜는
 *       "20260702"(yyyyMMdd), 금액은 "40,000" 같은 콤마 포함 문자열. 해외이용 거래도
 *       원화 환산된 최종 결제금액으로 "일시불" 시트에 이미 포함돼 있고, "해외이용" 시트는
 *       참고용 상세 내역이라 별도로 읽지 않는다 — 그래서 "일시불" 시트 행은 전부 그대로
 *       가져온다.</li>
 *   <li>구 포맷: 시트 하나에 이용대금 명세서 전체(합계/일시불/해외이용 상세)가 들어있고,
 *       날짜는 "26.07.02", 금액은 숫자 셀. 이 포맷은 해외이용 상세가 같은 시트 안에 별도
 *       섹션으로 다시 나열되므로, 그 (일자, 가맹점)과 일치하는 행은 중복으로 보고 제외한다.</li>
 * </ul>
 */
@Component
public class SamsungCardStatementParser implements StatementParser {

    private static final String NEW_FORMAT_SHEET_DOMESTIC = "일시불";
    private static final Pattern NEW_DATE_PATTERN = Pattern.compile("\\d{8}");
    private static final String NEW_HEADER_DATE = "이용일";
    private static final String NEW_HEADER_MERCHANT = "가맹점";
    private static final String NEW_HEADER_AMOUNT = "이용금액";

    private static final Pattern OLD_DATE_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}");
    private static final String OLD_HEADER_DATE = "이용일자";
    private static final String OLD_HEADER_MERCHANT = "이용가맹점";
    private static final String OLD_HEADER_AMOUNT = "이용금액(원)";
    private static final String OLD_HEADER_FOREIGN_RECEIVED_DATE = "접수일자";

    @Override
    public ImportProvider provider() {
        return ImportProvider.SAMSUNG_CARD;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getSheet(NEW_FORMAT_SHEET_DOMESTIC) != null) {
                return parseNewFormat(workbook);
            }
            return parseOldFormat(workbook.getSheetAt(0));
        } catch (IOException e) {
            throw ApiException.badRequest("엑셀 파일을 읽을 수 없습니다.");
        }
    }

    private List<ParsedTransaction> parseNewFormat(Workbook workbook) {
        Sheet domesticSheet = workbook.getSheet(NEW_FORMAT_SHEET_DOMESTIC);

        ExcelParsingUtils.HeaderRow header = ExcelParsingUtils.findHeaderRow(domesticSheet,
                Set.of(NEW_HEADER_DATE, NEW_HEADER_MERCHANT, NEW_HEADER_AMOUNT));
        int dateCol = header.column(NEW_HEADER_DATE);
        int merchantCol = header.column(NEW_HEADER_MERCHANT);
        int amountCol = header.column(NEW_HEADER_AMOUNT);

        List<ParsedTransaction> transactions = new ArrayList<>();
        for (int r = header.rowIndex() + 1; r <= domesticSheet.getLastRowNum(); r++) {
            Row row = domesticSheet.getRow(r);
            if (row == null) {
                continue;
            }
            String dateText = ExcelParsingUtils.stringValue(row.getCell(dateCol)).trim();
            if (dateText.isEmpty()) {
                continue;
            }
            if (!NEW_DATE_PATTERN.matcher(dateText).matches()) {
                break;
            }

            String merchant = ExcelParsingUtils.stringValue(row.getCell(merchantCol)).trim();
            BigDecimal amount = ExcelParsingUtils.numericValue(row.getCell(amountCol));
            if (merchant.isEmpty() || amount == null || amount.signum() <= 0) {
                continue;
            }

            int year = Integer.parseInt(dateText.substring(0, 4));
            int month = Integer.parseInt(dateText.substring(4, 6));
            int day = Integer.parseInt(dateText.substring(6, 8));
            transactions.add(new ParsedTransaction(LocalDate.of(year, month, day), merchant, amount));
        }
        return transactions;
    }

    private List<ParsedTransaction> parseOldFormat(Sheet sheet) {
        Set<String> foreignKeys = findExclusionKeys(sheet, OLD_DATE_PATTERN, OLD_HEADER_DATE,
                OLD_HEADER_MERCHANT, OLD_HEADER_FOREIGN_RECEIVED_DATE);

        ExcelParsingUtils.HeaderRow header = ExcelParsingUtils.findHeaderRow(sheet,
                Set.of(OLD_HEADER_DATE, OLD_HEADER_MERCHANT, OLD_HEADER_AMOUNT));
        int dateCol = header.column(OLD_HEADER_DATE);
        int merchantCol = header.column(OLD_HEADER_MERCHANT);
        int amountCol = header.column(OLD_HEADER_AMOUNT);

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
            if (!OLD_DATE_PATTERN.matcher(dateText).matches()) {
                break;
            }

            String merchant = ExcelParsingUtils.stringValue(row.getCell(merchantCol)).trim();
            BigDecimal amount = ExcelParsingUtils.numericValue(row.getCell(amountCol));
            if (merchant.isEmpty() || amount == null || amount.signum() <= 0) {
                continue;
            }
            if (foreignKeys.contains(exclusionKey(dateText, merchant))) {
                continue;
            }

            String[] parts = dateText.split("\\.");
            int year = 2000 + Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            transactions.add(new ParsedTransaction(LocalDate.of(year, month, day), merchant, amount));
        }
        return transactions;
    }

    /** 해외이용 쪽에 등장하는 (일자, 가맹점) 조합을 모은다. 해당 섹션/시트가 없으면 빈 집합. */
    private Set<String> findExclusionKeys(Sheet sheet, Pattern datePattern, String dateHeader, String merchantHeader,
            String... extraAnchorHeaders) {
        Set<String> required = new HashSet<>(Set.of(dateHeader, merchantHeader));
        required.addAll(Set.of(extraAnchorHeaders));

        var headerOpt = ExcelParsingUtils.findHeaderRowOptional(sheet, required);
        if (headerOpt.isEmpty()) {
            return Set.of();
        }
        ExcelParsingUtils.HeaderRow header = headerOpt.get();
        int dateCol = header.column(dateHeader);
        int merchantCol = header.column(merchantHeader);

        Set<String> keys = new HashSet<>();
        for (int r = header.rowIndex() + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            String dateText = ExcelParsingUtils.stringValue(row.getCell(dateCol)).trim();
            if (dateText.isEmpty()) {
                continue;
            }
            if (!datePattern.matcher(dateText).matches()) {
                break;
            }
            String merchant = ExcelParsingUtils.stringValue(row.getCell(merchantCol)).trim();
            if (!merchant.isEmpty()) {
                keys.add(exclusionKey(dateText, merchant));
            }
        }
        return keys;
    }

    private String exclusionKey(String dateText, String merchant) {
        return dateText + "|" + merchant;
    }
}
