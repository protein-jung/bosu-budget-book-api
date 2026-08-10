package com.bosu.housebook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.molit")
public record MolitProperties(String apiKey) {
}
