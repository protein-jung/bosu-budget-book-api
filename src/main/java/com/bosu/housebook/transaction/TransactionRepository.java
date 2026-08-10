package com.bosu.housebook.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByHouseholdIdAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            Long householdId, LocalDate from, LocalDate to);

    List<Transaction> findByHouseholdIdAndTransactionDateBetweenAndIsBackfillFalseOrderByTransactionDateAscIdAsc(
            Long householdId, LocalDate from, LocalDate to);

    List<Transaction> findByHouseholdIdAndCardIdAndTransactionDateBetween(
            Long householdId, Long cardId, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdAndHouseholdId(Long id, Long householdId);

    Optional<Transaction> findFirstByHouseholdIdOrderByTransactionDateAsc(Long householdId);
}
