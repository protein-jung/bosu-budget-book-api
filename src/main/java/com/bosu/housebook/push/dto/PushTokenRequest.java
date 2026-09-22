package com.bosu.housebook.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushTokenRequest(@NotBlank @Size(max = 200) String token) {
}
