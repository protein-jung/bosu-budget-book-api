package com.bosu.housebook.statistics;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.statistics.dto.MonthCommentRequest;
import com.bosu.housebook.statistics.dto.MonthCommentResponse;
import com.bosu.housebook.statistics.dto.MonthlySummaryResponse;
import com.bosu.housebook.statistics.dto.RangeSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/month-comments")
    public List<MonthCommentResponse> getMonthComments(@CurrentUserId Long userId, @RequestParam int year,
            @RequestParam int month) {
        return statisticsService.getComments(userId, year, month);
    }

    @PostMapping("/month-comments")
    public ResponseEntity<MonthCommentResponse> addMonthComment(@CurrentUserId Long userId,
            @Valid @RequestBody MonthCommentRequest request) {
        MonthCommentResponse response = statisticsService.addComment(userId, request.year(), request.month(),
                request.body());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/month-comments/{commentId}")
    public ResponseEntity<Void> deleteMonthComment(@CurrentUserId Long userId, @PathVariable Long commentId) {
        statisticsService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
