package com.mint.ping.service;

import com.mint.ping.PingApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.when;

@SpringBootTest(classes = {PingApplication.class})
class PingServiceTest {

    @Autowired
    private PingService pingService;

    @Test
    void startPing() {
        Assertions.assertDoesNotThrow(() -> {
            pingService.startPing();
        });
    }
}