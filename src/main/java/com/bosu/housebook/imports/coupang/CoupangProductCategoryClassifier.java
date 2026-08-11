package com.bosu.housebook.imports.coupang;

import com.bosu.housebook.imports.MerchantCategoryClassifier.CategorySuggestion;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 쿠팡 주문내역의 "상품명"으로 카테고리를 추정한다. {@link com.bosu.housebook.imports.MerchantCategoryClassifier}는
 * 가맹점명(상호) 키워드 기준이라 "커피", "피자" 같은 단어가 상품명에 그대로 들어간 식료품(우유·냉동피자 등)을
 * 외식비로 잘못 묶는 문제가 있어, 상품명 어휘에 맞춘 별도 규칙을 쓴다. 규칙에 안 걸리면 미분류로 남긴다
 * (애매하면 잘못 묶는 것보다 미분류가 안전하다는 원칙은 동일).
 */
@Component
public class CoupangProductCategoryClassifier {

    private record Rule(String categoryName, String color, String icon, List<String> keywords) {
        boolean matches(String upperProduct) {
            return keywords.stream().anyMatch(upperProduct::contains);
        }
    }

    private static final List<Rule> RULES = List.of(
            new Rule("아기용품", "#f08c00", "🐾", List.of(
                    "유아", "아동", "젖꼭지", "젖병", "기저귀", "분유", "이유식", "신생아", "쪽쪽이", "베베",
                    "속싸개", "아기", "주니어")),
            new Rule("식료품", "#2f9e44", "🥬", List.of(
                    "우유", "요거트", "치즈", "당근", "당면", "시리얼", "아이스크림", "계란", "김치", "라면",
                    "생수", "국내산", "냉동", "과자", "빵", "떡", "채소", "과일", "고기", "수산", "쌀", "간식")),
            new Rule("생필품", "#f08c00", "🧻", List.of(
                    "휴지", "티슈", "물티슈", "세제", "샴푸", "바디워시", "키친타월", "쓰레기봉투")));

    public Optional<CategorySuggestion> classify(String productName) {
        String upper = productName.toUpperCase(Locale.ROOT);
        return RULES.stream()
                .filter(rule -> rule.matches(upper))
                .findFirst()
                .map(rule -> new CategorySuggestion(rule.categoryName(), rule.color(), rule.icon()));
    }
}
