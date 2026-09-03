package com.bosu.housebook.featurerequest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRequestRepository extends JpaRepository<FeatureRequest, Long> {

    List<FeatureRequest> findAllByOrderByCreatedAtDesc();
}
