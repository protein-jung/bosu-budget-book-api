package com.bosu.housebook.transaction;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.transaction.dto.TransactionCommentRequest;
import com.bosu.housebook.transaction.dto.TransactionCommentResponse;
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

    @GetMapping("/search")
    public List<TransactionResponse> search(@CurrentUserId Long userId, @RequestParam String q) {
        return transactionService.search(userId, q);
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

    @GetMapping("/{transactionId}/comments")
    public List<TransactionCommentResponse> getComments(@CurrentUserId Long userId,
            @PathVariable Long transactionId) {
        return transactionService.getComments(userId, transactionId);
    }

    @PostMapping("/{transactionId}/comments")
    public ResponseEntity<TransactionCommentResponse> addComment(@CurrentUserId Long userId,
            @PathVariable Long transactionId, @Valid @RequestBody TransactionCommentRequest request) {
        TransactionCommentResponse response = transactionService.addComment(userId, transactionId, request.body());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{transactionId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@CurrentUserId Long userId, @PathVariable Long transactionId,
            @PathVariable Long commentId) {
        transactionService.deleteComment(userId, transactionId, commentId);
        return ResponseEntity.noContent().build();
    }
}
