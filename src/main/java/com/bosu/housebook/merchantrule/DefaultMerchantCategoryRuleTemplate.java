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
 * 가계부 생성 시 시드할 기본 가맹점 분류 규칙(모든 가계부가 공유하는 공통 템플릿). 실제 시드는
 * {@link MerchantRuleDefaultSeeder}가 이 테이블을 읽어 household 소유의
 * {@link MerchantCategoryRule} 행으로 복제한다.
 */
@Entity
@Table(name = "default_merchant_category_rule_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultMerchantCategoryRuleTemplate extends BaseTimeEntity {

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
}
