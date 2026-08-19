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
            new Rule("병원비", "#e03131", "🏥", List.of("병원", "의원", "치과", "한의원")),
            new Rule("약국", "#e03131", "💊", List.of("약국")),
            new Rule("영양제", "#e03131", "💊", List.of("종근당","유산균","비타민","칼슘")),
            new Rule("멤버십 비용", "#0c8599", "💳",
                    List.of("APPLE.COM", "APPLE", "CURSOR", "가비아", "넷플릭스", "NETFLIX", "GOOGLE", "로켓와우클럽")),
            new Rule("식료품", "#f08c00", "🥬", List.of("GS25", "지에스(GS)25", "(CU)", "세븐일레븐", "7-ELEVEN", "이마트24", "미니스톱", "농업협동조합","이마트", "홈플러스", "롯데마트", "생활협동조합", "한살림",
            "대파","마늘","블루베리","바나나","켈로그","하림","오이","딸기","포도","사과","배","감자","호박","토마토","양파","비엔나","귤","복숭아","오뚜기","고메"
            "비비고","한우","한돈","올리브유","미원","소금","오비","카스","콜라","사이다","포테토칩","해태제과","두부","메론","오리온","서울우유","치즈","선진 포크한돈","롯데웰푸드","미역","고구마","올품","삼립")),
            new Rule("생필품", "#f08c00", "🛒", List.of("다이소", "올리브영","ALIBABA", "TEMU", "테무","칫솔","치약","샴푸","화장지","매트","생리대")),
            new Rule("육아", "#f08c00", "👶", List.of("분유","젖병","젖꼭지","아기","유아")),
            new Rule("음식점", "#e64980", "🍽️", List.of("김밥", "치킨", "피자", "식당", "분식", "맥도날드")),
            new Rule("배달음식", "#e64980", "🛵", List.of(
                    "우아한형제들", "배민", "요기요", "쿠팡이츠")),
            new Rule("카페", "#e64980", "☕", List.of(
                    "카페", "커피", "스타벅스", "이디야", "투썸", "커피빈","공차",
                    "파리바게뜨", "뚜레쥬르", "베이커리")),
            new Rule("통신비", "#1971c2", "📱", List.of("LGU+", "LG U+", "SKT", "프리텔레콤", "알뜰폰")),
            new Rule("관리비", "#495057", "🧾", List.of("관리비", "도시가스", "한국전력", "에너지서비스")),
            new Rule("자동차 충전비", "#e03131", "⚡", List.of("볼트업")),
            new Rule("톨게이트 비용", "#2f9e44", "⛽", List.of("하이패스")),
            new Rule("대중교통", "#2f9e44", "⛽", List.of("버스", "지하철", "택시")),
            new Rule("오락/취미", "#e03131", "⚡", List.of( "오락실", "노래방", "PC방", "영화관", "CGV", "롯데시네마","뽑아핑")),
            new Rule("미용", "#e03131", "⚡", List.of("헤어", "미용실", "네일")));

    public Optional<CategorySuggestion> classify(String merchantName) {
        String upper = merchantName.toUpperCase(Locale.ROOT);
        return RULES.stream()
                .filter(rule -> rule.matches(upper))
                .findFirst()
                .map(rule -> new CategorySuggestion(rule.categoryName(), rule.color(), rule.icon()));
    }
}
