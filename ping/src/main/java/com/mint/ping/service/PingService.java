package com.mint.ping.service;

import com.mint.ping.util.ProcessLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PingService {

    @Value("${ping.pong-service-url}")
    private String pongServiceUrl;

    @Value("${lock.max-lock-number}")
    private int maxLockNumber;

    @Value("${lock.time-window-nano}")
    private int timeWindowNano;

    @Value("${lock.lock-file-name}")
    private String lockFileName;

    private final String SEND_WORD = "Hello";

    public String startPing() {
        ProcessLock lock = new ProcessLock(maxLockNumber,timeWindowNano,lockFileName);
        boolean success = lock.tryLock();
        if (success) {
            log.info("got lock & send request");
        }
        try {
            if (success) {
                Mono<String> str = invokePong();
                String answer = str.block();
                if ("error".equals(answer) == false) {
                    log.info("Request sent & Pong Respond.");
                }
                return answer;
            } else {
                log.info("Request not send as being “rate limited”.");
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public Mono<String> invokePong() {
        WebClient webClient = WebClient.builder().build();
        return webClient.post()
                .uri(pongServiceUrl)
                .bodyValue(SEND_WORD)
                .retrieve()
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals
                        , response -> {
                            log.info("Request send & Pong throttled it.");
                            return response.bodyToMono(String.class).map(Exception::new);
                        })
                .bodyToMono(String.class)
                .onErrorReturn("error");
    }
}
