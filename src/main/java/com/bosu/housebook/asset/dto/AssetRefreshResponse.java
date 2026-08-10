package com.bosu.housebook.asset.dto;

import java.util.List;

public record AssetRefreshResponse(int updatedCount, int failedCount, List<AssetResponse> assets) {
}
