package com.mint.pong.handler;

import com.mint.pong.service.AnswerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class HelloWorldHandler {

    @Value("${pong.time-window-nano}")
    private int timeWindowNano;

    @Autowired
    private AnswerService service;

    private final ReentrantLock lock = new ReentrantLock();
    private final String LOCK_TIME_KEY = "lock_time";


    private ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>(1);

    public Mono<ServerResponse> sayWorld(ServerRequest serverRequest) {
        lock.lock();
        try {
            long nanoTime = System.nanoTime();
            if (map.size() == 0 || (nanoTime >= map.get(LOCK_TIME_KEY) + timeWindowNano)) {
                String word = service.sayWorld();
                map.clear();
                map.put(LOCK_TIME_KEY, nanoTime);
                return ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(BodyInserters.fromValue(word));
            } else {
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.TEXT_PLAIN)
                        .build();
            }
        } finally {
            lock.unlock();
        }
    }
}