package com.mint.ping.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * 进程锁，用于在多个进程间实现节流控制
 */
@Slf4j
@Component
public class ProcessLock implements Serializable {

    // 允许的最大锁定次数
    private final int MAX_LOCK_NUMBER = 2;

    // 进程锁时间窗口
    private final int TIME_WINDOW_NANO = 1000_000_000;

    // 进程锁使用的文件名
    private final String LOCK_FILE_NAME = "lock.txt";

    // 锁定时间
    private long lockTime;

    // 锁定次数
    private int lockNumber;

    // canLock 为 false时，锁不可用，不可以获取锁
    private boolean canLock = true;
    private RandomAccessFile file = null;
    private FileChannel channel = null;
    private FileLock lock = null;

    public ProcessLock() {
        try {
            file = new RandomAccessFile(LOCK_FILE_NAME, "rw");
            channel = file.getChannel();
        } catch (FileNotFoundException e) {
            canLock = false;
        }
    }

    public synchronized boolean tryLock() {
        if (!canLock) {
            return false;
        }
        try {
            lock = channel.tryLock(1, 10, false);
            // 获取文件锁失败
            if (lock == null) {
                return false;
            }

            // 空文件代表第一次上锁，获取进程锁成功
            if (file.length() == 0) {
                this.updateLock();
                return true;
            }

            file.seek(0);
            this.lockTime = file.readLong();
            this.lockNumber = file.read();

            long nanoTime = System.nanoTime();

            // 时间窗口内已达到最大锁定次数，进程锁获取失败
            if (nanoTime < this.lockTime + TIME_WINDOW_NANO
                    && this.lockNumber >= MAX_LOCK_NUMBER) {
                return false;
            }

            // 时间窗口内未达到最大锁定次数，进程锁获取失败
            this.updateLock();
            return true;
        } catch (Exception e) {
            log.error("获取进程锁出错");
        }
        return false;
    }

    public synchronized void unlock() {
        try {
            if (lock != null) {
                lock.release();
            }
            if (file != null) {
                file.close();
            }
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            log.error("释放进程锁出错");
        }
    }

    private boolean updateLock() {
        try {
            long nanoTime = System.nanoTime();
            if (file.length() == 0 ||
                    nanoTime >= this.lockTime + TIME_WINDOW_NANO) {
                // 当前时间超过锁定时间窗口，设置全新的进程锁
                this.file.seek(0);
                file.writeLong(nanoTime);
                file.write(1);
            } else if (nanoTime < this.lockTime + TIME_WINDOW_NANO
                    && this.lockNumber < MAX_LOCK_NUMBER) {
                // 当前时间窗口未达到最大锁定次数
                this.file.seek(0);
                file.writeLong(this.lockTime);
                file.write(this.lockNumber + 1);
            }
            return true;
        } catch (IOException e) {
            log.error("更新进程锁出错");
        }
        return false;
    }
}
