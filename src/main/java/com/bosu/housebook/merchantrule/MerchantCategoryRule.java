package com.bosu.housebook.merchantrule;

import com.bosu.housebook.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가맹점명/쿠팡 상품명에 포함된 키워드로 지출 카테고리를 추정하는 규칙. 모든 가계부가 공유하는
 * 공통 데이터라 household에 속하지 않는다. 가맹점명 매칭에는 {@link #keywords}만, 쿠팡 상품명
 * 매칭에는 {@link #keywords}와 {@link #productKeywords}를 함께 쓴다(자세한 이유는
 * {@code com.bosu.housebook.imports.MerchantCategoryClassifier} 참고).
 * <p>
 * 여러 규칙에 동시에 걸리면 {@link #sortOrder}가 가장 앞선 규칙을 쓰므로, 겹칠 수 있는 규칙은
 * 순서에 유의해서 관리해야 한다.
 */
@Entity
@Table(name = "merchant_category_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantCategoryRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    private String color;

    private String icon;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "keywords_json", nullable = false, columnDefinition = "text")
    private List<String> keywords;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "product_keywords_json", nullable = false, columnDefinition = "text")
    private List<String> productKeywords;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public MerchantCategoryRule(String categoryName, String color, String icon, List<String> keywords,
            List<String> productKeywords, int sortOrder) {
        this.categoryName = categoryName;
        this.color = color;
        this.icon = icon;
        this.keywords = keywords;
        this.productKeywords = productKeywords;
        this.sortOrder = sortOrder;
    }
}
