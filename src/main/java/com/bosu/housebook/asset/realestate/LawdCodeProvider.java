package com.bosu.housebook.asset.realestate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** 아파트 실거래가 API에서 쓰는 시군구 단위(5자리) 법정동코드 목록을 리소스에서 읽어 들고 있는다. */
@Component
public class LawdCodeProvider {

    private final List<LawdCode> codes;

    public LawdCodeProvider() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new ClassPathResource("lawd-codes.json").getInputStream()) {
            this.codes = objectMapper.readValue(inputStream, new TypeReference<List<LawdCode>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("법정동코드 리소스를 읽을 수 없습니다.", e);
        }
    }

    public List<LawdCode> all() {
        return codes;
    }
}
