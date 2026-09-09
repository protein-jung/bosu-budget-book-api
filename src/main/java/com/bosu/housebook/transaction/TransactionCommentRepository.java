package com.bosu.housebook.transaction;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCommentRepository extends JpaRepository<TransactionComment, Long> {

    List<TransactionComment> findByTransactionIdOrderByCreatedAtAsc(Long transactionId);
}
