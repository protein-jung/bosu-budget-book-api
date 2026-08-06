package com.bosu.housebook.transaction;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.transaction.dto.TransactionRequest;
import com.bosu.housebook.transaction.dto.TransactionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getMonthly(@CurrentUserId Long userId, @RequestParam int year,
            @RequestParam int month) {
        return transactionService.getMonthly(userId, year, month);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(userId, request));
    }

    @PutMapping("/{transactionId}")
    public TransactionResponse update(@CurrentUserId Long userId, @PathVariable Long transactionId,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(userId, transactionId, request);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long transactionId) {
        transactionService.delete(userId, transactionId);
        return ResponseEntity.noContent().build();
    }
}
