package com.bosu.housebook.asset;

public enum AssetType {
    REAL_ESTATE,
    VEHICLE,
    STOCK,
    CRYPTO,
    CASH,
    OTHER;

    public boolean isLivePriced() {
        return this == STOCK || this == CRYPTO;
    }
}
