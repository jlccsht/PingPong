package com.mint.ping.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * <p>Process lock, use for rate limit control between multi process in same device.</p>
 *
 * <p>Process lock based {@link FileLock} class. Successful acquisition of process lock
 * depends on the result of comparision of  lockTime of long and lockNumber of int
 * in lock file and current time, <tt>TIME_WINDOW_NANO</tt>, <tt>MAX_LOCK_NUMBER</tt></p>
 */
@Slf4j
public class ProcessLock implements Serializable {

    /** Maximum number of locks allowed */
    private int maxLockNumber = 2;

    /** time window of process lock, in milliseconds */
    private int timeWindowNano = 1000_000_000;

    /** file name of process lock */
    private String lockFileName = "run/lock.txt";

    /** time of process lock acquired */
    private long lockTime;

    /** number of process lock used in same time window */
    private int lockNumber;

    /**
     * use for lock control
     * when false, never got lock
     * when true, the lock is ready for acquire
     */
    private boolean canLock = true;
    private RandomAccessFile file = null;
    private FileChannel channel = null;
    private FileLock lock = null;
    private boolean status;

    public boolean isStatus() {
        return status;
    }

    public ProcessLock() {
        init(this.lockFileName);
    }

    public ProcessLock(int maxLockNumber, int timeWindowNano, String lockFileName ) {
        this.maxLockNumber = maxLockNumber;
        this.timeWindowNano = timeWindowNano;
        this.lockFileName = lockFileName;

        init(this.lockFileName);
    }

    private void init(String lockFileName) {
        try {
            file = new RandomAccessFile(lockFileName, "rw");
            channel = file.getChannel();
        } catch (FileNotFoundException e) {
            canLock = false;
        }
    }

    public synchronized boolean tryLock() {
        if (!canLock) {
            status = false;
            return false;
        }
        try {
            lock = channel.tryLock(1, 10, false);
            if (lock == null) { // Failed to acquire file lock
                status = false;
                return false;
            }

            // process lock can acquire as lock file is empty
            if (file.length() == 0) {
                status = true;
                this.updateLock();
                return true;
            }

            file.seek(0);
            this.lockTime = file.readLong();
            this.lockNumber = file.read();

            long nanoTime = System.nanoTime();

            // number of lock in current time window reach max value, can not acquire lock
            if (nanoTime < this.lockTime + timeWindowNano
                    && this.lockNumber >= maxLockNumber) {
                status = false;
                return false;
            }

            status = true;
            // update lock info when lock acquired
            this.updateLock();
            return true;
        } catch (Exception e) {
            log.error("Error acquiring process lock.");
        }
        return false;
    }

    public synchronized void unlock() {
        try {
            status = false;
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
            log.error("Error releasing process lock.");
        }
    }

    private boolean updateLock() {
        try {
            long nanoTime = System.nanoTime();
            if (file.length() == 0 ||
                    nanoTime >= this.lockTime + timeWindowNano) {
                // when current time exceed lock time window, set up a new process lock.
                this.file.seek(0);
                file.writeLong(nanoTime);
                file.write(1);
            } else if (nanoTime < this.lockTime + timeWindowNano
                    && this.lockNumber < maxLockNumber) {
                // The current time window has not reach the maximum number of locks.
                this.file.seek(0);
                file.writeLong(this.lockTime);
                file.write(this.lockNumber + 1);
            }
            return true;
        } catch (IOException e) {
            log.error("An error occured with the update process lock.");
        }
        return false;
    }
}
