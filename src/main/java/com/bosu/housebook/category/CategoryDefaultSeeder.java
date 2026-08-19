package com.bosu.housebook.category;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.household.Household;
import java.util.List;
import org.springframework.stereotype.Component;

/** 가계부 생성 시 엑셀 가계부 구조에 맞춘 대분류/소분류 트리를 시드한다. */
@Component
public class CategoryDefaultSeeder {

    private record Leaf(String name, String color, String icon) {
    }

    private record Group(String name, String color, String icon, List<Leaf> children) {
    }

    private static final List<Group> EXPENSE_GROUPS = List.of(
            new Group("생활비", "#e64980", "🛍️", List.of(
                    new Leaf("외식비", "#e64980", "🍽️"),
                    new Leaf("식대/생필품", "#f08c00", "🛒"),
                    new Leaf("통신비/인터넷", "#1971c2", "📱"),
                    new Leaf("멤버십 비용", "#0c8599", "💳"))),
            new Group("주거비", "#0c8599", "🏠", List.of(
                    new Leaf("집 원금", "#0c8599", "🏦"),
                    new Leaf("집 이자", "#1971c2", "📉"),
                    new Leaf("관리비", "#495057", "🧾"))),
            new Group("자동차", "#2f9e44", "🚗", List.of(
                    new Leaf("충전비/통비", "#2f9e44", "⛽"),
                    new Leaf("보험", "#495057", "🛡️"),
                    new Leaf("세금", "#e03131", "💰"),
                    new Leaf("주차비", "#7048e8", "🅿️"))),
            new Group("수민", "#1971c2", "👛", List.of(
                    new Leaf("개인용돈", "#1971c2", "💵"),
                    new Leaf("계모임", "#7048e8", "🤝"))),
            new Group("보영", "#e64980", "👛", List.of(
                    new Leaf("개인용돈", "#e64980", "💵"),
                    new Leaf("계모임", "#7048e8", "🤝"))),
            new Group("혜린", "#f08c00", "👛", List.of(
                    new Leaf("햇살이용품", "#f08c00", "🐾"))),
            new Group("미래준비", "#7048e8", "💰", List.of(
                    new Leaf("적금", "#7048e8", "🏦"),
                    new Leaf("수민 IRP", "#1971c2", "📈"),
                    new Leaf("투자모으기", "#2f9e44", "📊"),
                    new Leaf("코인", "#f08c00", "🪙"))),
            new Group("의료", "#e03131", "🏥", List.of(
                    new Leaf("병원비", "#e03131", "🏥"),
                    new Leaf("약국", "#e03131", "💊"),
                    new Leaf("영양제", "#e03131", "🌿"),
                    new Leaf("수민보험", "#1971c2", "👤"),
                    new Leaf("보영보험", "#e64980", "👤"),
                    new Leaf("해린", "#f08c00", "👤"))),
            new Group("기타", "#495057", "📦", List.of(
                    new Leaf("카드 연회비", "#495057", "💳"),
                    new Leaf("경조사비", "#e64980", "🎁"),
                    new Leaf("특수지출", "#e03131", "⚡"))));

    private static final List<Leaf> INCOME_LEAVES = List.of(
            new Leaf("급여", "#2f9e44", "💵"),
            new Leaf("기타수입", "#1971c2", "💰"));

    private final CategoryRepository categoryRepository;

    public CategoryDefaultSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private static final String FUTURE_PREP_GROUP_NAME = "미래준비";

    public void seed(Household household) {
        int groupOrder = 0;
        for (Group group : EXPENSE_GROUPS) {
            boolean excludedFromExpenseStats = FUTURE_PREP_GROUP_NAME.equals(group.name());
            Category parent = categoryRepository.save(new Category(
                    household, group.name(), TransactionType.EXPENSE, group.color(), group.icon(), null,
                    groupOrder++, null, true, excludedFromExpenseStats));
            int leafOrder = 0;
            for (Leaf leaf : group.children()) {
                categoryRepository.save(new Category(
                        household, leaf.name(), TransactionType.EXPENSE, leaf.color(), leaf.icon(), parent,
                        leafOrder++, null, false, excludedFromExpenseStats));
            }
        }

        int incomeOrder = 0;
        for (Leaf leaf : INCOME_LEAVES) {
            categoryRepository.save(new Category(
                    household, leaf.name(), TransactionType.INCOME, leaf.color(), leaf.icon(), null, incomeOrder++));
        }
    }
}
