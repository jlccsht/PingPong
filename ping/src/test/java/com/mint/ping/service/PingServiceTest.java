package com.mint.ping.service;

import com.mint.ping.PingApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {PingApplication.class})
class PingServiceTest {

    @Autowired
    private PingService pingService;

    @BeforeAll
    public static void prepare() {
        try {
            Files.deleteIfExists(Paths.get("../run/lock.txt"));
        } catch (IOException e) {
        }
    }

    @Test
    void startPing() {
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 50; i++) {
                pingService.startPing();
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}