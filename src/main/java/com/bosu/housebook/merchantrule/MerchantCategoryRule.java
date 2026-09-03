package com.bosu.housebook.merchantrule;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가맹점명/쿠팡 상품명에 포함된 키워드로 지출 카테고리를 추정하는 규칙. household 소유라 가계부마다
 * 서로 다른 규칙을 가질 수 있다(새 가계부는 {@code MerchantRuleDefaultSeeder}가
 * {@link DefaultMerchantCategoryRuleTemplate}을 복제해 초기값으로 채운다). 가맹점명 매칭에는
 * {@link #keywords}만, 쿠팡 상품명 매칭에는 {@link #keywords}와 {@link #productKeywords}를
 * 함께 쓴다(자세한 이유는 {@code com.bosu.housebook.imports.MerchantCategoryClassifier} 참고).
 * <p>
 * 같은 household 안에서 여러 규칙에 동시에 걸리면 {@link #sortOrder}가 가장 앞선 규칙을 쓰므로,
 * 겹칠 수 있는 규칙은 순서에 유의해서 관리해야 한다.
 */
@Entity
@Table(name = "merchant_category_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantCategoryRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "keywords_json", nullable = false, columnDefinition = "text")
    private List<String> keywords;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "product_keywords_json", nullable = false, columnDefinition = "text")
    private List<String> productKeywords;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public MerchantCategoryRule(Household household, Category category, List<String> keywords,
            List<String> productKeywords, int sortOrder) {
        this.household = household;
        this.category = category;
        this.keywords = keywords;
        this.productKeywords = productKeywords;
        this.sortOrder = sortOrder;
    }

    public void update(Category category, List<String> keywords) {
        this.category = category;
        this.keywords = keywords;
    }
}
