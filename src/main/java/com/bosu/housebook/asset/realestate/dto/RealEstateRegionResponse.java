package com.bosu.housebook.asset.realestate.dto;

import java.util.List;

public record RealEstateRegionResponse(String sido, List<SigunguOption> sigunguList) {

    public record SigunguOption(String name, String code) {
    }
}
