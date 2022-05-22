package com.mint.ping.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessLockTest {

    @Test
    void tryLock() {
        ProcessLock lock = new ProcessLock();
        lock.tryLock();
        Assertions.assertTrue(lock.isStatus());
        lock.unlock();
        Assertions.assertFalse(lock.isStatus());
    }

    @Test
    void tryLock2() {
        ProcessLock lock = new ProcessLock(3, 2000_000_000, "run/lock.txt");
        lock.tryLock();
        Assertions.assertTrue(lock.isStatus());
        lock.unlock();
        Assertions.assertFalse(lock.isStatus());
    }

    @Test
    void unlock() {
        ProcessLock lock = new ProcessLock();
        lock.unlock();
        Assertions.assertFalse(lock.isStatus());
    }
}