package com.bosu.housebook.merchantrule;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.merchantrule.dto.MerchantCategoryRuleRequest;
import com.bosu.housebook.merchantrule.dto.MerchantCategoryRuleResponse;
import com.bosu.housebook.merchantrule.dto.UncategorizedMerchantResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant-category-rules")
public class MerchantCategoryRuleController {

    private final MerchantCategoryRuleService merchantCategoryRuleService;

    public MerchantCategoryRuleController(MerchantCategoryRuleService merchantCategoryRuleService) {
        this.merchantCategoryRuleService = merchantCategoryRuleService;
    }

    @GetMapping
    public List<MerchantCategoryRuleResponse> getAll(@CurrentUserId Long userId) {
        return merchantCategoryRuleService.getRules(userId);
    }

    @GetMapping("/uncategorized-merchants")
    public List<UncategorizedMerchantResponse> getUncategorizedMerchants(@CurrentUserId Long userId) {
        return merchantCategoryRuleService.getUncategorizedMerchants(userId);
    }

    @PostMapping
    public ResponseEntity<MerchantCategoryRuleResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody MerchantCategoryRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantCategoryRuleService.createRule(userId, request));
    }

    @PutMapping("/{ruleId}")
    public MerchantCategoryRuleResponse update(@CurrentUserId Long userId, @PathVariable Long ruleId,
            @Valid @RequestBody MerchantCategoryRuleRequest request) {
        return merchantCategoryRuleService.updateRule(userId, ruleId, request);
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long ruleId) {
        merchantCategoryRuleService.deleteRule(userId, ruleId);
        return ResponseEntity.noContent().build();
    }
}
