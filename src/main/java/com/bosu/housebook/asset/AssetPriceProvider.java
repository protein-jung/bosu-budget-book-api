package com.bosu.housebook.asset;

import java.util.Optional;

public interface AssetPriceProvider {

    Optional<QuotedPrice> fetchPrice(String symbol);
}
