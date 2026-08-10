package com.bosu.housebook.statistics;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.statistics.dto.MonthlySummaryResponse;
import com.bosu.housebook.statistics.dto.RangeSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public MonthlySummaryResponse getMonthlySummary(@CurrentUserId Long userId, @RequestParam int year,
            @RequestParam int month) {
        return statisticsService.getMonthlySummary(userId, year, month);
    }

    @GetMapping("/range")
    public RangeSummaryResponse getRangeSummary(
            @CurrentUserId Long userId,
            @RequestParam int fromYear,
            @RequestParam int fromMonth,
            @RequestParam int toYear,
            @RequestParam int toMonth) {
        return statisticsService.getRangeSummary(userId, fromYear, fromMonth, toYear, toMonth);
    }

    @GetMapping("/full-history")
    public RangeSummaryResponse getFullHistory(@CurrentUserId Long userId) {
        return statisticsService.getFullHistorySummary(userId);
    }
}
