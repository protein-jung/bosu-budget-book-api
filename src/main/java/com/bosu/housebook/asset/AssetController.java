package com.bosu.housebook.asset;

import com.bosu.housebook.asset.dto.AssetRefreshResponse;
import com.bosu.housebook.asset.dto.AssetRequest;
import com.bosu.housebook.asset.dto.AssetResponse;
import com.bosu.housebook.asset.dto.AssetSummaryResponse;
import com.bosu.housebook.asset.dto.StockSymbolCandidate;
import com.bosu.housebook.auth.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final StockSymbolSearchService stockSymbolSearchService;

    public AssetController(AssetService assetService, StockSymbolSearchService stockSymbolSearchService) {
        this.assetService = assetService;
        this.stockSymbolSearchService = stockSymbolSearchService;
    }

    @GetMapping
    public List<AssetResponse> getAll(@CurrentUserId Long userId) {
        return assetService.getAll(userId);
    }

    @GetMapping("/summary")
    public AssetSummaryResponse getSummary(@CurrentUserId Long userId) {
        return assetService.getSummary(userId);
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@CurrentUserId Long userId, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(userId, request));
    }

    @PutMapping("/{assetId}")
    public AssetResponse update(@CurrentUserId Long userId, @PathVariable Long assetId,
            @Valid @RequestBody AssetRequest request) {
        return assetService.update(userId, assetId, request);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long assetId) {
        assetService.delete(userId, assetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-prices")
    public AssetRefreshResponse refreshPrices(@CurrentUserId Long userId) {
        return assetService.refreshPrices(userId);
    }

    @GetMapping("/search-symbols")
    public List<StockSymbolCandidate> searchSymbols(@RequestParam String query) {
        return stockSymbolSearchService.search(query);
    }
}
