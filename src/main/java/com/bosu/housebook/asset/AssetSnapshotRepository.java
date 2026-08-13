package com.bosu.housebook.asset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {

    Optional<AssetSnapshot> findByHouseholdIdAndSnapshotDate(Long householdId, LocalDate snapshotDate);

    List<AssetSnapshot> findByHouseholdIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(Long householdId,
            LocalDate from);
}
