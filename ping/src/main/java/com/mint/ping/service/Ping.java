package com.mint.ping.service;

import com.mint.ping.util.ProcessLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class Ping {
    private final String ANSWER_SERVICE_URL = "http://localhost:8090/sayWorld";
    private final String SEND_WORD = "Hello";

    private ProcessLock lock = null;

    public synchronized void startPing() {
        lock = new ProcessLock();
        boolean success = this.lock.tryLock();
        try {

                if (success) {
                    log.info("got lock & send request");
                    Mono<String> str = invokePong();
                    String s = str.block();
                    if (StringUtils.hasText(s)) {
                        log.info("Request sent & Pong Respond.");
                    }
//                    return s;
                } else {
                    log.info("Request not send as being “rate limited”.");
                }

        } finally {
            lock.unlock();
        }
//        return null;
    }

    private Mono<String> invokePong() {
        return WebClient.builder().build().post()
                .uri(ANSWER_SERVICE_URL)
                .bodyValue(SEND_WORD)
                .retrieve()
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals
                        , response -> {
                            log.info("Request send & Pong throttled it.");
                            return response.bodyToMono(String.class).map(Exception::new);
                        })
                .bodyToMono(String.class);
    }
}
