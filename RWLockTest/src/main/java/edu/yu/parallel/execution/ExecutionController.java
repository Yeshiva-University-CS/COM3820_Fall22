package edu.yu.parallel.execution;


public interface ExecutionController {
    /**
     * Block until all threads have started
     */
    void awaitForAllThreadsToHaveStarted();

    /**
     * Block until current thread has permission to lock
     */
    void awaitPermissionToLock();

    /**
     * Block until current thread is able to complete execution
     */
    void awaitPermissionToCompleteExecution();

    /**
     * Signal the execution object that it can now request a lock
     */
    void permitLockRequest();

    /**
     * Signal the execution object that it can complete its execution
     * and unlock the lock
     */
    void completeExecution();

    /**
     * Request the reader lock for the current thread
     */
    void lockForRead();

    /**
     * Request the writer lock for the current thread
     */
    void lockForWrite();

    /**
     * Unlock the current lock
     */
    void unlock();
}