package com.bosu.housebook.household.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinHouseholdRequest(@NotBlank String inviteCode) {
}
