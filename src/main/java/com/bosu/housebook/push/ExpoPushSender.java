package com.bosu.housebook.push;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** 앱 알림(Notification)이 만들어질 때 그 사용자의 등록된 기기로 Expo 푸시 알림을 같이 보낸다.
 * Expo 계정/API 키가 따로 필요 없는 Expo Push API(https://exp.host)를 그대로 쓴다. 응답
 * 지연을 주지 않도록 비동기로 보내고, 실패해도 알림 생성 자체에는 영향 없게 예외를 삼킨다. */
@Component
public class ExpoPushSender {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushSender.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestClient restClient;

    public ExpoPushSender() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Async
    public void send(List<String> tokens, String title, String body, String link) {
        if (tokens.isEmpty()) {
            return;
        }
        try {
            List<Map<String, Object>> messages = tokens.stream()
                    .map(token -> Map.<String, Object>of(
                            "to", token,
                            "title", title,
                            "body", body != null ? body : "",
                            "data", link != null ? Map.of("link", link) : Map.of()))
                    .toList();
            restClient.post()
                    .uri(EXPO_PUSH_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Expo 푸시 전송에 실패했습니다.", e);
        }
    }
}
