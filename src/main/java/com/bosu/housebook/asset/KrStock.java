package com.bosu.housebook.asset;

/** market: "KOSPI" 또는 "KOSDAQ". */
public record KrStock(String name, String code, String market) {

    public String yahooSymbol() {
        return code + ("KOSPI".equals(market) ? ".KS" : ".KQ");
    }
}
