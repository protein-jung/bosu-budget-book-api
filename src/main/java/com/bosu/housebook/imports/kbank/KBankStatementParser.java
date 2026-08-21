package com.bosu.housebook.imports.kbank;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.imports.ExcelParsingUtils;
import com.bosu.housebook.imports.ImportProvider;
import com.bosu.housebook.imports.ParsedTransaction;
import com.bosu.housebook.imports.StatementParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * 케이뱅크는 두 가지 명세서를 지원한다. 파일 첫 바이트로 형식을 구분해 같은 "케이뱅크" 출처
 * 하나로 둘 다 받는다.
 *
 * <ul>
 *   <li>PDF "이용대금명세서": 거래일자/상품명/카드번호/가맹점명/이용금액 표를 텍스트로 뽑아
 *       한 줄씩 정규식으로 파싱한다. 카드번호가 "1234-56**-****-7890" 형태로 고정돼 있어 이를
 *       기준선 삼아 그 앞은 상품명, 뒤는 가맹점명/금액으로 나눈다. 전부 지출로 들어간다.</li>
 *   <li>엑셀 "계좌 거래내역": 거래일시/입금금액/출금금액/적요내용 열을 읽어, 입금금액이 있으면
 *       수입, 출금금액이 있으면 지출로 나눈다. 적요내용은 거래 메모로 그대로 쓴다. 거래구분이
 *       "체크결제"인 행은 체크카드 결제라 같은 금액·같은 날짜로 PDF 이용대금명세서에도 찍혀 있을
 *       수 있는데, 거기서는 가맹점명으로 뜨고 여기서는 적요내용으로 떠서 메모 텍스트가 서로 다를
 *       수 있다. 그래서 이 행들은 메모를 무시하고 날짜·금액·수입지출만으로 중복을 판정하도록
 *       {@link ParsedTransaction#looseDuplicateCheck()}를 표시해둔다(그래도 첫 업로드라면 그냥
 *       그대로 들어온다). 적요내용이 "모두 다 캐시백"인 행(케이뱅크 제휴 캐시백 적립)은 실제
 *       수입·지출이 아니라 잡음이라 아예 가져오지 않는다.</li>
 * </ul>
 */
@Component
public class KBankStatementParser implements StatementParser {

    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F'};

    private static final DateTimeFormatter PDF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern PDF_ROW_PATTERN = Pattern.compile(
            "^(\\d{4}\\.\\d{2}\\.\\d{2})\\s+.+?\\s+\\d{4}-\\d{2}\\*{2}-\\*{4}-\\d{4}\\s+(.+?)\\s+(-?[\\d,]+)$");

    private static final String TRANSFER_HEADER_DATE = "거래일시";
    private static final String TRANSFER_HEADER_KIND = "거래구분";
    private static final String TRANSFER_HEADER_DEPOSIT = "입금금액";
    private static final String TRANSFER_HEADER_WITHDRAWAL = "출금금액";
    private static final String TRANSFER_HEADER_DESCRIPTION = "적요내용";
    private static final String TRANSFER_KIND_CHECK_PAYMENT = "체크결제";
    private static final String TRANSFER_DESCRIPTION_IGNORED_CASHBACK = "모두 다 캐시백";
    private static final DateTimeFormatter TRANSFER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern TRANSFER_DATE_PATTERN = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}");

    @Override
    public ImportProvider provider() {
        return ImportProvider.KBANK;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream) {
        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw ApiException.badRequest("파일을 읽을 수 없습니다.");
        }
        return isPdf(bytes) ? parsePdf(bytes) : parseTransferExcel(bytes);
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private List<ParsedTransaction> parsePdf(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(document);
            List<ParsedTransaction> transactions = new ArrayList<>();
            for (String line : text.split("\\R")) {
                Matcher matcher = PDF_ROW_PATTERN.matcher(line.trim());
                if (!matcher.matches()) {
                    continue;
                }
                String merchant = matcher.group(2).trim();
                BigDecimal amount = parseAmount(matcher.group(3));
                if (merchant.isEmpty() || amount.signum() <= 0) {
                    continue;
                }
                LocalDate date = LocalDate.parse(matcher.group(1), PDF_DATE_FORMAT);
                transactions.add(new ParsedTransaction(date, merchant, amount));
            }
            return transactions;
        } catch (IOException e) {
            throw ApiException.badRequest("PDF 파일을 읽을 수 없습니다.");
        }
    }

    private List<ParsedTransaction> parseTransferExcel(byte[] bytes) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            ExcelParsingUtils.HeaderRow header = ExcelParsingUtils.findHeaderRow(sheet,
                    Set.of(TRANSFER_HEADER_DATE, TRANSFER_HEADER_KIND, TRANSFER_HEADER_DEPOSIT,
                            TRANSFER_HEADER_WITHDRAWAL, TRANSFER_HEADER_DESCRIPTION));
            int dateCol = header.column(TRANSFER_HEADER_DATE);
            int kindCol = header.column(TRANSFER_HEADER_KIND);
            int depositCol = header.column(TRANSFER_HEADER_DEPOSIT);
            int withdrawalCol = header.column(TRANSFER_HEADER_WITHDRAWAL);
            int descriptionCol = header.column(TRANSFER_HEADER_DESCRIPTION);

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
                if (!TRANSFER_DATE_PATTERN.matcher(dateText).find()) {
                    break;
                }

                String kind = ExcelParsingUtils.stringValue(row.getCell(kindCol)).trim();
                boolean looseDuplicateCheck = TRANSFER_KIND_CHECK_PAYMENT.equals(kind);

                String description = ExcelParsingUtils.stringValue(row.getCell(descriptionCol)).trim();
                if (TRANSFER_DESCRIPTION_IGNORED_CASHBACK.equals(description)) {
                    continue;
                }
                BigDecimal deposit = ExcelParsingUtils.numericValue(row.getCell(depositCol));
                BigDecimal withdrawal = ExcelParsingUtils.numericValue(row.getCell(withdrawalCol));

                TransactionType type;
                BigDecimal amount;
                if (deposit != null && deposit.signum() > 0) {
                    type = TransactionType.INCOME;
                    amount = deposit;
                } else if (withdrawal != null && withdrawal.signum() > 0) {
                    type = TransactionType.EXPENSE;
                    amount = withdrawal;
                } else {
                    continue;
                }
                if (description.isEmpty()) {
                    continue;
                }

                LocalDate date = LocalDate.parse(dateText.substring(0, 10), TRANSFER_DATE_FORMAT);
                transactions.add(new ParsedTransaction(date, description, amount, type, looseDuplicateCheck));
            }
            return transactions;
        } catch (IOException e) {
            throw ApiException.badRequest("엑셀 파일을 읽을 수 없습니다.");
        }
    }

    private BigDecimal parseAmount(String text) {
        return new BigDecimal(text.replace(",", ""));
    }
}
