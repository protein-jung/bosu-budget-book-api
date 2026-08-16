package com.bosu.housebook.asset;

public enum AssetType {
    REAL_ESTATE,
    VEHICLE,
    STOCK,
    CRYPTO,
    GOLD,
    SILVER,
    CASH,
    LOAN,
    OTHER;

    public boolean isLivePriced() {
        return this == STOCK || this == CRYPTO;
    }
}
