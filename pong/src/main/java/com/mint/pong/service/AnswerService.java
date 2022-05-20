package com.mint.pong.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.rmi.MarshalledObject;
import java.util.Date;

@Service
public class AnswerService {
    public String sayWorld() {
        return "World";
    }
}
