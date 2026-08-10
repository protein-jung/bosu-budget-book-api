package com.bosu.housebook.card;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByHouseholdIdOrderByIdAsc(Long householdId);

    Optional<Card> findByIdAndHouseholdId(Long id, Long householdId);

    Optional<Card> findByHouseholdIdAndNameIgnoreCase(Long householdId, String name);
}
