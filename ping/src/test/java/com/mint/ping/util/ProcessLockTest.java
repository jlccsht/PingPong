package com.mint.ping.util;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProcessLockTest {
    @Value("${lock.lock-file-name}")
    private String lockFileName;

    ProcessLock lock = null;

    @BeforeAll
    public static void prepare() {
        try {
            Files.deleteIfExists(Paths.get("../run/lock.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lockFileUsed() {
        FileLock otherLock = null;
        try {
            RandomAccessFile file = new RandomAccessFile("../run/lock3.txt", "rw");
            FileChannel channel = file.getChannel();
            otherLock = channel.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        lock = new ProcessLock(2, 1000_000_000, "../run/lock3.txt");
        ;
        boolean bLock = lock.tryLock();
        Assertions.assertFalse(bLock);
        lock.unlock();
        try {
            if (otherLock != null) {
                otherLock.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    public void tryLockUntilFalse() {
        boolean bLock = true;
        for (int i = 0; i < 20; i++) {
            lock = new ProcessLock(2, 1000_000_000, "../run/lock3.txt");
            bLock = lock.tryLock();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
        if (lock != null) {
            lock.unlock();
        }
        Assertions.assertFalse(lock.isStatus());
    }

    @Test
    void tryLock() {
        lock = new ProcessLock(3, 2000_000_000, "Z:/run/lock2.txt");
        lock.tryLock();
        Assertions.assertFalse(lock.isStatus());
        lock.unlock();
        Assertions.assertFalse(lock.isStatus());
    }
}