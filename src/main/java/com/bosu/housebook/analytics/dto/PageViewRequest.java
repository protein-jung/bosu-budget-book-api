package com.bosu.housebook.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageViewRequest(
        @NotBlank @Size(max = 255) String path,
        @NotBlank @Size(max = 64) String visitorId,
        @Size(max = 100) String utmSource,
        @Size(max = 100) String utmMedium,
        @Size(max = 100) String utmCampaign) {
}
