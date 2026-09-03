package com.bosu.housebook.featurerequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeatureRequestReplyRequest(@NotBlank @Size(max = 4000) String reply) {
}
