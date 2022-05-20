package com.mint.ping.service;

import com.mint.ping.PingApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {PingApplication.class})
class PingTest {

    @Autowired
    private Ping ping;

    @Test
    void startPing() {
        Assertions.assertDoesNotThrow(() -> {
            ping.startPing();
        });

    }
}