package edu.yu.parallel.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

abstract public class ControlledExecution<T> implements Callable<T> {
    private final static Logger logger = LogManager.getLogger(ControlledExecution.class);
    private final String id;
    private final int sequenceNum;
    private final ExecutionController controller;
    private final Callable<T> callable;
    private volatile LockStatus lockStatus = LockStatus.NONE;

    protected ControlledExecution(String id, int sequenceNum, ExecutionController controller, Callable<T> callable) {
        this.id = id;
        this.sequenceNum = sequenceNum;
        this.controller = controller;
        this.callable = callable;
    }

    public String getId() {
        return this.id;
    }

    public LockStatus getLockStatus() {
        return this.lockStatus;
    }

    public void permitLocking() {
        controller.permitLockRequest();
    }

    public void completeExecution() {
        controller.completeExecution();
    }

    public T call() throws Exception {
        // Task is in LockStatus.NONE
        // Wait for all threads to have reached the starting line of the race
        logProgress("Thread is started");
        waitSomeTime();
        controller.awaitForAllThreadsToHaveStarted();

        lockStatus = LockStatus.READY;
        logProgress("Thread is ready to go");

        // To control the order of locking, each thread will wait
        // for explicit permission to request the lock
        controller.awaitPermissionToLock();

        // Once permission to request the lock is granted,
        // request the lock
        lockStatus = LockStatus.WAITING;
        logProgress("Attempting to get the lock");
        this.lock();

        // The thread has the lock and continues with execution
        lockStatus = LockStatus.LOCKED;

        try {
            logProgress("Lock obtained");
            var result = callable.call();

            // To control when the lock is released, each thread will
            // wait for explicit permission to complete its execution
            controller.awaitPermissionToCompleteExecution();

            // Permission to finish is granted. Thread will unlock,
            // return the result and exit
            // Status will be LockStatus.UNLOCKED
            logProgress("Completing execution");
            return result;
        } finally {
            controller.unlock();
            lockStatus = LockStatus.UNLOCKED;
            logProgress("Lock released");
        }
    }

    /**
     * Override in subclass to call create a reader or a writer
     * by calling either lockForRead or lockForWrite
     */
    abstract protected void lock();

    protected void lockForRead() {
        controller.lockForRead();
    }

    protected void lockForWrite() {
        controller.lockForWrite();
    }

    private void logProgress(String msg) {
        logger.info("{}:{}, {}", id, lockStatus, msg);
    }

    private void waitSomeTime() {
        try {
            Thread.sleep(Math.max(500 - (50 * sequenceNum), 0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public enum LockStatus {
        NONE, READY, WAITING, LOCKED, UNLOCKED
    }
}
