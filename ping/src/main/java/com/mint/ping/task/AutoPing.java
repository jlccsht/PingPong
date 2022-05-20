package com.mint.ping.task;

import com.mint.ping.service.Ping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AutoPing implements Runnable {
    private final int REQUEST_INTERVAL_MILLISECONDS = 100;

    @Autowired
    private Ping ping;

    public void setNumber(Integer number) {
        this.number = number;
    }

    private Integer number;

    @PostConstruct
    private void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            if (this.number!= null) {
                i++;
                if (i > number) {
                    break;
                }
            }
            ping.startPing();
            try {
                TimeUnit.MILLISECONDS.sleep(REQUEST_INTERVAL_MILLISECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}
