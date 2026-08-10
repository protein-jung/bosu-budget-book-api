package com.bosu.housebook.asset.realestate;

import com.bosu.housebook.asset.realestate.dto.AddressCandidateResponse;
import com.bosu.housebook.asset.realestate.dto.RealEstateRegionResponse;
import com.bosu.housebook.asset.realestate.dto.RealEstateTradeResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/real-estate")
public class RealEstateController {

    private final RealEstateTradeService realEstateTradeService;

    public RealEstateController(RealEstateTradeService realEstateTradeService) {
        this.realEstateTradeService = realEstateTradeService;
    }

    @GetMapping("/regions")
    public List<RealEstateRegionResponse> getRegions() {
        return realEstateTradeService.getRegions();
    }

    @GetMapping("/address-search")
    public List<AddressCandidateResponse> searchAddresses(@RequestParam String query) {
        return realEstateTradeService.searchAddresses(query);
    }

    @GetMapping("/trades")
    public List<RealEstateTradeResponse> getTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYm,
            @RequestParam(required = false) String complexName) {
        return realEstateTradeService.searchTrades(lawdCd, dealYm, complexName);
    }
}
