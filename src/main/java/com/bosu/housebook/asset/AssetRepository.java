package com.bosu.housebook.asset;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByHouseholdIdOrderByIdAsc(Long householdId);

    Optional<Asset> findByIdAndHouseholdId(Long id, Long householdId);
}
