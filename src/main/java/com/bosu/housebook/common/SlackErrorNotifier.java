package com.bosu.housebook.common;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * 예상치 못한 서버 오류를 슬랙 웹훅으로 보낸다. {@code app.slack.error-webhook-url}이 비어있으면
 * (로컬에서 아직 설정 안 한 경우 등) 조용히 건너뛴다 — 메일/외부 API 키가 없을 때와 같은 방식.
 * 응답 지연을 주지 않도록 비동기로 보내고, 전송 자체가 실패해도 예외를 삼키고 로그만 남긴다.
 */
@Component
public class SlackErrorNotifier {

    private static final Logger log = LoggerFactory.getLogger(SlackErrorNotifier.class);
    private static final int MAX_STACK_TRACE_LENGTH = 2500;

    private final RestClient restClient;

    @Value("${app.slack.error-webhook-url:}")
    private String webhookUrl;

    @Value("${app.env:local}")
    private String env;

    public SlackErrorNotifier() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Async
    public void notify(HttpServletRequest request, Throwable throwable) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", buildMessage(request, throwable)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("슬랙 에러 알림 전송에 실패했습니다.", e);
        }
    }

    private String buildMessage(HttpServletRequest request, Throwable throwable) {
        String envLabel = "prod".equalsIgnoreCase(env) ? "상용" : "로컬";
        String requestInfo = request != null ? request.getMethod() + " " + request.getRequestURI() : "알 수 없음";
        return "*[%s] API 에러* `%s`\n예외: `%s`\n메시지: %s\n```%s```".formatted(
                envLabel, requestInfo, throwable.getClass().getName(),
                String.valueOf(throwable.getMessage()), summarize(throwable));
    }

    private String summarize(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String full = sw.toString();
        return full.length() > MAX_STACK_TRACE_LENGTH ? full.substring(0, MAX_STACK_TRACE_LENGTH) + "..." : full;
    }
}
