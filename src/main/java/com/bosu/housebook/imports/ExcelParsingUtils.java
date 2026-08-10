package com.bosu.housebook.imports;

import com.bosu.housebook.common.ApiException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/** 카드사/기관마다 다른 엑셀 명세서 포맷을 파싱할 때 공통으로 쓰는 헬퍼. */
public final class ExcelParsingUtils {

    private ExcelParsingUtils() {
    }

    public record HeaderRow(Map<String, Integer> columnIndexByHeader, int rowIndex) {
        public int column(String header) {
            return columnIndexByHeader.get(header);
        }
    }

    /** 시트를 순회하며 요구된 헤더 문자열이 모두 한 행에 등장하는 지점을 찾는다. */
    public static HeaderRow findHeaderRow(Sheet sheet, Set<String> requiredHeaders) {
        return findHeaderRowOptional(sheet, requiredHeaders)
                .orElseThrow(() -> ApiException.badRequest("명세서 형식을 인식할 수 없습니다."));
    }

    /** 해당 섹션이 명세서에 아예 없을 수도 있는 경우(예: 해외이용 없는 달)에 쓰는 버전. */
    public static Optional<HeaderRow> findHeaderRowOptional(Sheet sheet, Set<String> requiredHeaders) {
        for (Row row : sheet) {
            Map<String, Integer> found = new HashMap<>();
            for (Cell cell : row) {
                String value = stringValue(cell).trim();
                if (requiredHeaders.contains(value)) {
                    found.put(value, cell.getColumnIndex());
                }
            }
            if (found.keySet().containsAll(requiredHeaders)) {
                return Optional.of(new HeaderRow(found, row.getRowNum()));
            }
        }
        return Optional.empty();
    }

    public static String stringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    public static BigDecimal numericValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().replaceAll("[^0-9.]", "");
            if (text.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
