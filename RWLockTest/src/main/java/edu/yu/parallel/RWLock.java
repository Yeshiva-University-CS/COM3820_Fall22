package edu.yu.parallel;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RWLock implements RWLockInterface {
    final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public RWLock() {
    }

    @Override
    public void lockRead() {
        rwLock.readLock().lock();
    }

    @Override
    public void lockWrite() {
        rwLock.writeLock().lock();
    }

    @Override
    public synchronized void unlock() throws IllegalMonitorStateException {
        if (rwLock.getReadLockCount() > 0) rwLock.readLock().unlock();
        else if (rwLock.getWriteHoldCount() > 0) rwLock.writeLock().unlock();
        else throw new IllegalMonitorStateException();
    }

} // class
