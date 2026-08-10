package com.bosu.housebook.asset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 한국거래소 상장법인목록(코스피/코스닥, kind.krx.co.kr에서 받은 공개 데이터를 정적으로 번들)을
 * 들고 있는다. Yahoo Finance 종목 검색이 한글 검색어를 거부하기 때문에, 국내 종목은 이 목록에서
 * 이름/코드로 직접 찾는다.
 */
@Component
public class KrStockProvider {

    private final List<KrStock> stocks;

    public KrStockProvider() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new ClassPathResource("kr-stocks.json").getInputStream()) {
            this.stocks = objectMapper.readValue(inputStream, new TypeReference<List<KrStock>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("국내 종목 리소스를 읽을 수 없습니다.", e);
        }
    }

    public List<KrStock> all() {
        return stocks;
    }
}
