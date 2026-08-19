package com.bosu.housebook.imports.kbank;

import com.bosu.housebook.common.ApiException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * 케이뱅크 "이용대금명세서" PDF: 거래일자/상품명/카드번호/가맹점명/이용금액 표를 텍스트로 뽑아
 * 한 줄씩 정규식으로 파싱한다. 카드번호가 "1234-56**-****-7890" 형태로 고정돼 있어 이를 기준선
 * 삼아 그 앞은 상품명, 뒤는 가맹점명/금액으로 나눈다. 취소·환불로 금액이 음수인 행은 다른
 * 카드사 파서와 동일하게 제외한다.
 */
@Component
public class KBankStatementParser implements StatementParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern ROW_PATTERN = Pattern.compile(
            "^(\\d{4}\\.\\d{2}\\.\\d{2})\\s+.+?\\s+\\d{4}-\\d{2}\\*{2}-\\*{4}-\\d{4}\\s+(.+?)\\s+(-?[\\d,]+)$");

    @Override
    public ImportProvider provider() {
        return ImportProvider.KBANK;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            String text = new PDFTextStripper().getText(document);
            List<ParsedTransaction> transactions = new ArrayList<>();
            for (String line : text.split("\\R")) {
                Matcher matcher = ROW_PATTERN.matcher(line.trim());
                if (!matcher.matches()) {
                    continue;
                }
                String merchant = matcher.group(2).trim();
                BigDecimal amount = parseAmount(matcher.group(3));
                if (merchant.isEmpty() || amount.signum() <= 0) {
                    continue;
                }
                LocalDate date = LocalDate.parse(matcher.group(1), DATE_FORMAT);
                transactions.add(new ParsedTransaction(date, merchant, amount));
            }
            return transactions;
        } catch (IOException e) {
            throw ApiException.badRequest("PDF 파일을 읽을 수 없습니다.");
        }
    }

    private BigDecimal parseAmount(String text) {
        return new BigDecimal(text.replace(",", ""));
    }
}
