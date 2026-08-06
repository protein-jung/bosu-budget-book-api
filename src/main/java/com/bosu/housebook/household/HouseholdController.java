package com.bosu.housebook.household;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.household.dto.HouseholdCreateRequest;
import com.bosu.housebook.household.dto.HouseholdResponse;
import com.bosu.housebook.household.dto.JoinHouseholdRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping
    public ResponseEntity<HouseholdResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody HouseholdCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(householdService.create(userId, request));
    }

    @GetMapping("/me")
    public HouseholdResponse getMyHousehold(@CurrentUserId Long userId) {
        return householdService.getMyHousehold(userId);
    }

    @GetMapping("/invite-code")
    public Map<String, String> getInviteCode(@CurrentUserId Long userId) {
        return Map.of("inviteCode", householdService.getInviteCode(userId));
    }

    @PostMapping("/join")
    public HouseholdResponse join(@CurrentUserId Long userId, @Valid @RequestBody JoinHouseholdRequest request) {
        return householdService.join(userId, request);
    }
}
