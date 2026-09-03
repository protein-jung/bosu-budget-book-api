package com.bosu.housebook.featurerequest;

import com.bosu.housebook.featurerequest.dto.AdminFeatureRequestResponse;
import com.bosu.housebook.featurerequest.dto.FeatureRequestReplyRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/admin/**}는 SecurityConfig에서 ROLE_ADMIN으로 이미 막혀 있어서 이 컨트롤러
 * 자체엔 별도 권한 체크가 필요 없다. */
@RestController
@RequestMapping("/api/admin/feature-requests")
public class AdminFeatureRequestController {

    private final FeatureRequestService featureRequestService;

    public AdminFeatureRequestController(FeatureRequestService featureRequestService) {
        this.featureRequestService = featureRequestService;
    }

    @GetMapping
    public List<AdminFeatureRequestResponse> getAll() {
        return featureRequestService.getAllForAdmin();
    }

    @PostMapping("/{requestId}/reply")
    public AdminFeatureRequestResponse reply(@PathVariable Long requestId,
            @Valid @RequestBody FeatureRequestReplyRequest request) {
        return featureRequestService.reply(requestId, request);
    }
}
