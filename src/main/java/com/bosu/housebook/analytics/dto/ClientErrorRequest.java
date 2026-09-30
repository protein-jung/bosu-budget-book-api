package com.bosu.housebook.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientErrorRequest(
        @Size(max = 10) String method,
        @Size(max = 500) String url,
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 30) String platform,
        @Size(max = 40) String occurredAt) {
}
