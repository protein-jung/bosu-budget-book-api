package com.bosu.housebook.imports;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    boolean existsByHouseholdIdAndFileChecksum(Long householdId, String fileChecksum);
}
