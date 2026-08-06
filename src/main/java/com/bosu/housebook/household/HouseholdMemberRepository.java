package com.bosu.housebook.household;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, Long> {

    Optional<HouseholdMember> findByUserId(Long userId);

    List<HouseholdMember> findByHouseholdId(Long householdId);

    boolean existsByHouseholdIdAndUserId(Long householdId, Long userId);
}
