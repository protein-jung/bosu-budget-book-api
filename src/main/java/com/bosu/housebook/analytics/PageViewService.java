package com.bosu.housebook.analytics;

import com.bosu.housebook.admin.dto.AdminPageViewStatResponse;
import com.bosu.housebook.admin.dto.AdminPageViewsResponse;
import com.bosu.housebook.admin.dto.AdminTrendPointResponse;
import com.bosu.housebook.analytics.dto.PageViewRequest;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PageViewService {

    private final PageViewRepository pageViewRepository;
    private final UserRepository userRepository;

    public PageViewService(PageViewRepository pageViewRepository, UserRepository userRepository) {
        this.pageViewRepository = pageViewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void record(PageViewRequest request, Long userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        pageViewRepository.save(new PageView(request.path(), request.visitorId(), user));
    }

    /** 어드민 "접속 통계" 화면. 최근 days일 동안의 페이지별 조회수·순 방문자와 일별 조회수 추이. */
    public AdminPageViewsResponse adminStats(int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        LocalDateTime from = start.atStartOfDay();

        List<AdminPageViewStatResponse> byPath = pageViewRepository.findPathStatsSince(from).stream()
                .map(p -> new AdminPageViewStatResponse(p.getPath(), p.getViews(), p.getUniqueVisitors()))
                .toList();

        Map<LocalDate, Long> dailyTotals = new HashMap<>();
        for (var row : pageViewRepository.findDailyTotalsSince(from)) {
            dailyTotals.put(row.getDay(), row.getTotal());
        }
        List<AdminTrendPointResponse> daily = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            daily.add(new AdminTrendPointResponse(date.toString(), BigDecimal.valueOf(dailyTotals.getOrDefault(date, 0L))));
        }

        long totalViews = pageViewRepository.countByCreatedAtGreaterThanEqual(from);
        long totalUniqueVisitors = pageViewRepository.countDistinctVisitorsSince(from);

        return new AdminPageViewsResponse(totalViews, totalUniqueVisitors, byPath, daily);
    }
}
