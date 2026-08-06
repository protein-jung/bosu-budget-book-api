package com.bosu.housebook.household.dto;

import com.bosu.housebook.household.HouseholdMember;
import com.bosu.housebook.household.HouseholdRole;

public record MemberResponse(Long userId, String name, String email, HouseholdRole role) {

    public static MemberResponse from(HouseholdMember member) {
        return new MemberResponse(
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getEmail(),
                member.getRole());
    }
}
