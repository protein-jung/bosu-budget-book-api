package com.bosu.housebook.featurerequest;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.featurerequest.dto.FeatureRequestCreateRequest;
import com.bosu.housebook.featurerequest.dto.FeatureRequestResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feature-requests")
public class FeatureRequestController {

    private final FeatureRequestService featureRequestService;

    public FeatureRequestController(FeatureRequestService featureRequestService) {
        this.featureRequestService = featureRequestService;
    }

    @GetMapping
    public List<FeatureRequestResponse> getAll() {
        return featureRequestService.getAll();
    }

    @PostMapping
    public ResponseEntity<FeatureRequestResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody FeatureRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(featureRequestService.create(userId, request));
    }
}
