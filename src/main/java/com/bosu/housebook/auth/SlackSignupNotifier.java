package com.bosu.housebook.auth;

import com.bosu.housebook.user.User;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 새 회원가입을 슬랙 웹훅으로 알린다. {@code app.slack.signup-webhook-url}이 비어있으면
 * (로컬에서 아직 설정 안 한 경우 등) 조용히 건너뛴다 — SlackErrorNotifier와 같은 방식.
 * 응답 지연을 주지 않도록 비동기로 보내고, 전송 자체가 실패해도 예외를 삼키고 로그만 남긴다.
 */
@Component
public class SlackSignupNotifier {

    private static final Logger log = LoggerFactory.getLogger(SlackSignupNotifier.class);

    private final RestClient restClient;

    @Value("${app.slack.signup-webhook-url:}")
    private String webhookUrl;

    @Value("${app.env:local}")
    private String env;

    public SlackSignupNotifier() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Async
    public void notify(User user) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", buildMessage(user)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("슬랙 회원가입 알림 전송에 실패했습니다.", e);
        }
    }

    private String buildMessage(User user) {
        String envLabel = "prod".equalsIgnoreCase(env) ? "상용" : "로컬";
        return "*[%s] 새 회원가입* %s (%s)".formatted(envLabel, user.getName(), user.getEmail());
    }
}
