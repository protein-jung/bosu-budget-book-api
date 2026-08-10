package com.bosu.housebook.imports;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 가맹점명에 포함된 키워드로 대략적인 지출 카테고리를 추정한다. 규칙에 걸리지 않으면
 * 빈 Optional을 반환하고, 호출부에서 "미분류"로 처리한다. 실수로 잘못 묶이는 것보다
 * 애매하면 미분류로 남기는 쪽이 안전하므로 키워드는 비교적 구체적인 것만 사용한다.
 * <p>
 * 카테고리명은 엑셀 기반 기본 소분류(리프) 이름과 맞춘다.
 */
@Component
public class MerchantCategoryClassifier {

    public record CategorySuggestion(String categoryName, String color, String icon) {
    }

    private record Rule(String categoryName, String color, String icon, List<String> keywords) {
        boolean matches(String upperMerchant) {
            return keywords.stream().anyMatch(upperMerchant::contains);
        }
    }

    private static final List<Rule> RULES = List.of(
            new Rule("병원약", "#e03131", "💊", List.of("병원", "의원", "치과", "한의원", "조리원", "약국")),
            new Rule("멤버십 비용", "#0c8599", "💳",
                    List.of("APPLE.COM", "APPLE", "CURSOR", "가비아", "넷플릭스", "NETFLIX", "GOOGLE")),
            new Rule("식대/생필품", "#f08c00", "🛒", List.of("GS25", "(CU)", "세븐일레븐", "7-ELEVEN", "이마트24", "미니스톱", "다이소")),
            new Rule("외식비", "#e64980", "🍽️", List.of(
                    "카페", "커피", "스타벅스", "이디야", "투썸", "커피빈",
                    "파리바게뜨", "뚜레쥬르", "베이커리", "김밥", "치킨", "피자", "식당", "분식",
                    "우아한형제들", "배민", "요기요", "쿠팡이츠")),
            new Rule("통신비/인터넷", "#1971c2", "📱", List.of("엘지유플러스", "LGU+", "SKT", "프리텔레콤", "알뜰폰")),
            new Rule("관리비", "#495057", "🧾", List.of("관리비", "도시가스", "한국전력", "에너지서비스")),
            new Rule("특수지출", "#e03131", "⚡", List.of("세입금", "재산세", "국세", "지방세", "과태료", "지자체")),
            new Rule("충전비/통비", "#2f9e44", "⛽", List.of("하이패스", "버스", "지하철", "주유소", "택시", "충전")),
            new Rule("식대/생필품", "#f08c00", "🛒", List.of("이마트", "홈플러스", "롯데마트", "생활협동조합", "한살림", "쿠팡", "11번가", "지마켓", "옥션", "ALIBABA", "TEMU", "테무")),
            new Rule("특수지출", "#e03131", "⚡", List.of("헤어", "미용실", "네일", "피부과", "오락실", "노래방", "PC방", "영화관", "CGV", "롯데시네마")),
            new Rule("햇살이용품", "#f08c00", "🐾", List.of("육아", "어린이집", "유치원", "학원", "펫", "동물병원")));

    public Optional<CategorySuggestion> classify(String merchantName) {
        String upper = merchantName.toUpperCase(Locale.ROOT);
        return RULES.stream()
                .filter(rule -> rule.matches(upper))
                .findFirst()
                .map(rule -> new CategorySuggestion(rule.categoryName(), rule.color(), rule.icon()));
    }
}
