package com.bosu.housebook.recurringexpense;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.recurringexpense.dto.RecurringExpenseActiveRequest;
import com.bosu.housebook.recurringexpense.dto.RecurringExpenseRequest;
import com.bosu.housebook.recurringexpense.dto.RecurringExpenseResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recurring-expenses")
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    public RecurringExpenseController(RecurringExpenseService recurringExpenseService) {
        this.recurringExpenseService = recurringExpenseService;
    }

    @GetMapping
    public List<RecurringExpenseResponse> getAll(@CurrentUserId Long userId) {
        return recurringExpenseService.getAll(userId);
    }

    @PostMapping
    public ResponseEntity<RecurringExpenseResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody RecurringExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringExpenseService.create(userId, request));
    }

    @PutMapping("/{recurringExpenseId}")
    public RecurringExpenseResponse update(@CurrentUserId Long userId, @PathVariable Long recurringExpenseId,
            @Valid @RequestBody RecurringExpenseRequest request) {
        return recurringExpenseService.update(userId, recurringExpenseId, request);
    }

    @PatchMapping("/{recurringExpenseId}/active")
    public RecurringExpenseResponse setActive(@CurrentUserId Long userId, @PathVariable Long recurringExpenseId,
            @Valid @RequestBody RecurringExpenseActiveRequest request) {
        return recurringExpenseService.setActive(userId, recurringExpenseId, request.active());
    }

    @DeleteMapping("/{recurringExpenseId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long recurringExpenseId) {
        recurringExpenseService.delete(userId, recurringExpenseId);
        return ResponseEntity.noContent().build();
    }
}
