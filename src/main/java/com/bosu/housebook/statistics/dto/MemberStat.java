package com.bosu.housebook.statistics.dto;

import java.math.BigDecimal;

public record MemberStat(Long userId, String userName, BigDecimal income, BigDecimal expense) {
}
