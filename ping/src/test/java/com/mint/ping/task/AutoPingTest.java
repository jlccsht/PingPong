package com.mint.ping.task;

import com.mint.ping.PingApplication;
import com.mint.ping.service.PingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = PingApplication.class)
class AutoPingTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private AutoPing autoPing;

    @Autowired
    private PingService ping;

    @Test
    @Order(1)
    void say() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            autoPing.setNumber(100);
            autoPing.run();
        });

        Map<String, Integer> map = new HashMap<>();
        String strPath = "D:\\MCA\\ping_pong\\PingPong\\ping\\ping-logs\\";
        try {
            logRealCheck(map, strPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    @DisplayName("rps is less than or eaqual to 2")
    void logCheck() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        String strPath = "D:\\MCA\\ping_pong\\PingPong\\run\\pinglogs\\";
        try {
            logRealCheck(map, strPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logRealCheck(Map<String, Integer> map,
                              String strPath) throws IOException {
        Path path = Paths.get(strPath);
        Files.list(path).forEach((f) -> {
            try {
                Files.lines(Paths.get(strPath + f.getFileName())).forEach((line) -> {
                    if (line.indexOf("INFO") > -1) {
                        String second = line.substring(0, line.indexOf("INFO") - 6);
                        String msg = line.substring(line.lastIndexOf(":") + 2);
                        if ("got lock & send request".equals(msg)) {
                            if (map.containsKey(second)) {
                                Integer num = map.get(second);
                                map.put(second, num + 1);
                            } else {
                                map.put(second, 1);
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        List<Map.Entry<String, Integer>> collect = map.entrySet().stream().collect(Collectors.toList());
        collect.sort((item1, item2) -> {
            return item1.getKey().compareTo(item2.getKey());
        });
        collect.stream().forEach(System.out::println);

        boolean bOK = true;
        for (Integer value : map.values()) {
            if (value > 2) {
                bOK = false;
            }
        }
        assertTrue(bOK);
    }
}