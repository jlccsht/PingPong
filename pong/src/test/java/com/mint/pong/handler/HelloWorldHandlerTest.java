package com.mint.pong.handler;

import com.mint.pong.PongApplication;
import com.mint.pong.service.AnswerService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PongApplication.class})
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class HelloWorldHandlerTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private HelloWorldHandler handler;

    @MockBean
    private AnswerService service;

    @Test
    @Order(1)
    void sayWorld1() throws Exception {
        // mock here
        when(service.sayWorld()).thenReturn("World");

        this.client
                .post()
                .uri("/sayWorld")
                .bodyValue("Hello")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("World");
    }

    @Test
    @Order(2)
    void sayWorld() {
        when(service.sayWorld()).thenReturn("World");
        ConcurrentHashMap<HttpStatus, Map<Long, Integer>> outerMap = getHttpStatusMapConcurrentHashMap();
        // 验证包含两个状态码
        assertEquals(2, outerMap.size());
        // 验证正常应答每秒不起过一次
        Iterator<Integer> iterator = outerMap.get(HttpStatus.OK).values().iterator();
        boolean bOK = true;
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            if (next > 1) {
                bOK = false;
                break;
            }
        }
        assertEquals(true, bOK);
    }

    @Test
    @Order(3)
    void sayWorld3() {
        when(service.sayWorld()).thenReturn("World");
        ConcurrentHashMap<HttpStatus, Map<Long, Integer>> outerMap = getHttpStatusMapConcurrentHashMap();

        Iterator<Integer> iterator2 = outerMap.get(HttpStatus.TOO_MANY_REQUESTS).values().iterator();
        boolean b429 = true;
        while (iterator2.hasNext()) {
            Integer next = iterator2.next();
            if (next > 1) {
                b429 = false;
                break;
            }
        }
        assertEquals(true, b429);
    }


    // 生成测试数据
    private ConcurrentHashMap<HttpStatus, Map<Long, Integer>> getHttpStatusMapConcurrentHashMap() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/sayWorld")
                .body("Hello");
        MockServerWebExchange exchange = MockServerWebExchange
                .from(request);
        ServerRequest other = ServerRequest
                        .create(exchange, HandlerStrategies.withDefaults().messageReaders());

        ConcurrentHashMap<HttpStatus, Map<Long, Integer>> outerMap = new ConcurrentHashMap<>();
        ExecutorService service = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 200; i++) {
            service.execute(() -> {
                Mono<ServerResponse> mono = handler.sayWorld(other);
                ServerResponse response = mono.block();
                HttpStatus status = response.statusCode();
                Map<Long, Integer> map = outerMap.getOrDefault(status, new HashMap<>());
                long currentSecond = System.currentTimeMillis() / 1000;
                map.putIfAbsent(currentSecond, map.get(currentSecond) == null ? 1 : map.get(currentSecond) + 1);
                outerMap.put(status, map);
            });
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return outerMap;
    }
}