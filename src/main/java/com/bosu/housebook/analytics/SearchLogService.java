package com.bosu.housebook.analytics;

import com.bosu.housebook.admin.dto.AdminSearchStatsResponse;
import com.bosu.housebook.admin.dto.AdminSearchTermResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchLogService {

    private static final int TOP_QUERY_LIMIT = 30;

    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;

    public SearchLogService(SearchLogRepository searchLogRepository, UserRepository userRepository) {
        this.searchLogRepository = searchLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void record(String query, Long userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        searchLogRepository.save(new SearchLog(query, user));
    }

    /** 어드민 "접속 통계" 화면의 인기 검색어 표. 최근 days일 동안 가장 많이 검색된 검색어 순. */
    public AdminSearchStatsResponse adminStats(int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1L).atStartOfDay();

        var topQueries = searchLogRepository.findTopQueriesSince(from, PageRequest.of(0, TOP_QUERY_LIMIT)).stream()
                .map(p -> new AdminSearchTermResponse(p.getQuery(), p.getCount()))
                .toList();

        return new AdminSearchStatsResponse(searchLogRepository.countByCreatedAtGreaterThanEqual(from), topQueries);
    }
}
