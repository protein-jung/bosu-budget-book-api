package com.bosu.housebook.imports.coupang;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.imports.ImportProvider;
import com.bosu.housebook.imports.ParsedTransaction;
import com.bosu.housebook.imports.StatementParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * 쿠팡 "주문내역 다운로드" CSV. 주문 단위가 아니라 품목(상품) 단위로 한 행씩 들어있고,
 * 배송비까지 반영된 "총결제금액(원)"이 그 품목의 실제 결제 금액이므로 그대로 사용한다.
 * 취소/반품된 품목("상태"에 CANCELED 또는 RETURN_COMPLETE 포함)은 실제 지출이 아니므로
 * 제외한다.
 */
@Component
public class CoupangOrderStatementParser implements StatementParser {

    private static final String HEADER_DATE = "주문일";
    private static final String HEADER_PRODUCT = "상품명";
    private static final String HEADER_AMOUNT = "총결제금액(원)";
    private static final String HEADER_STATUS = "상태";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm");

    @Override
    public ImportProvider provider() {
        return ImportProvider.COUPANG;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream) {
        try (Reader reader = stripBom(inputStream);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)) {
            List<ParsedTransaction> transactions = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (!record.isMapped(HEADER_DATE) || !record.isMapped(HEADER_PRODUCT)
                        || !record.isMapped(HEADER_AMOUNT) || !record.isMapped(HEADER_STATUS)) {
                    throw ApiException.badRequest("쿠팡 주문내역 CSV 형식을 인식할 수 없습니다.");
                }

                String status = record.get(HEADER_STATUS);
                if (status.contains("CANCELED") || status.contains("RETURN_COMPLETE")) {
                    continue;
                }

                String product = record.get(HEADER_PRODUCT).trim();
                BigDecimal amount = parseAmount(record.get(HEADER_AMOUNT));
                if (product.isEmpty() || amount == null || amount.signum() <= 0) {
                    continue;
                }

                LocalDate date = LocalDateTime.parse(record.get(HEADER_DATE).trim(), DATE_FORMAT).toLocalDate();
                transactions.add(new ParsedTransaction(date, product, amount));
            }
            return transactions;
        } catch (IOException e) {
            throw ApiException.badRequest("CSV 파일을 읽을 수 없습니다.");
        }
    }

    private BigDecimal parseAmount(String text) {
        String digits = text.replace(",", "").trim();
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Reader stripBom(InputStream inputStream) throws IOException {
        PushbackInputStream pushback = new PushbackInputStream(inputStream, 3);
        byte[] bom = new byte[3];
        int read = pushback.read(bom, 0, 3);
        if (read != 3 || bom[0] != (byte) 0xEF || bom[1] != (byte) 0xBB || bom[2] != (byte) 0xBF) {
            pushback.unread(bom, 0, Math.max(read, 0));
        }
        return new InputStreamReader(pushback, StandardCharsets.UTF_8);
    }
}
