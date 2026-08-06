package com.bosu.housebook.household.dto;

import com.bosu.housebook.household.Household;
import java.util.List;

public record HouseholdResponse(Long id, String name, String inviteCode, List<MemberResponse> members) {

    public static HouseholdResponse of(Household household, List<MemberResponse> members) {
        return new HouseholdResponse(household.getId(), household.getName(), household.getInviteCode(), members);
    }
}
