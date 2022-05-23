package com.mint.ping.task;

import com.mint.ping.service.PingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AutoPing implements Runnable {
    @Value("${ping.request-interval-milliseconds}")
    private int requestIntervalMilliseconds;

    @Value("${ping.max-fail-number}")
    private int maxFailNumber;

    @Autowired
    private PingService pingService;

    public void setNumber(Integer number) {
        this.number = number;
    }

    private Integer number;

    private int failNumer = 0;

    @PostConstruct
    private void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            if (this.number!= null) {
                if (i <= number) {
                    i++;
                } else {
                    break;
                }
            }
            String answer = pingService.startPing();

            try {
                TimeUnit.MILLISECONDS.sleep(requestIntervalMilliseconds);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}
