package com.bosu.housebook.recurringexpense;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    List<RecurringExpense> findByHouseholdIdOrderByDayOfMonthAscIdAsc(Long householdId);

    Optional<RecurringExpense> findByIdAndHouseholdId(Long id, Long householdId);

    List<RecurringExpense> findByActiveTrue();
}
