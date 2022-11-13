package edu.yu.parallel.execution;

import edu.yu.parallel.RWLockInterface;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * This class is **NOT** thread safe. It must only be used from a single thread.
 *
 * @param <T>
 */
abstract class AbstractExecutionGroup<T> {

    private final RWLockInterface rwLock;
    private final long defaultWaitTime;
    private final ArrayList<ControlledExecution> executionTasks = new ArrayList<>();

    protected AbstractExecutionGroup(RWLockInterface rwLock, long defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
        this.rwLock = rwLock;
    }

    protected RWLockInterface getRWLock() {
        return rwLock;
    }

    public Callable<T> createReaderTask(Callable<T> callable) {
        if (threadsAreReadyToLock())
            throw new IllegalComponentStateException("Cannot create more tasks after awaitAllThreadsStarted is called");
        var controller = this.newExecutionController();
        var task = new Reader<T>(executionTasks.size(), controller, callable);
        executionTasks.add(task);
        return task;
    }

    public Callable<T> createReaderTask() {
        return createReaderTask(() -> {
            return null;
        });
    }

    public Callable<T> createWriterTask(Callable<T> callable) {
        if (threadsAreReadyToLock())
            throw new IllegalComponentStateException("Cannot create more tasks after awaitAllThreadsStarted is called");
        var controller = this.newExecutionController();
        var task = new Writer<T>(executionTasks.size(), controller, callable);
        executionTasks.add(task);
        return task;
    }

    public Callable<T> createWriterTask() {
        return createWriterTask(() -> {
            return null;
        });
    }

    public void lockInOrder() {
        lockInOrder(this.defaultWaitTime);
    }

    public void lockInOrder(long sleepWaitTime) {
        if (!threadsAreReadyToLock())
            throw new IllegalComponentStateException("awaitAllThreadsStarted must be called first");

        for (int i = 0; i < executionTasks.size(); ++i) {
            executionTasks.get(i).permitLocking();
            do {
                try {
                    Thread.sleep(sleepWaitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (executionTasks.get(i).getLockStatus() == ControlledExecution.LockStatus.READY);
        }
    }

    public ControlledExecution.LockStatus getLockStatus(int seqNum) {
        return executionTasks.get(seqNum).getLockStatus();
    }

    public void completeExecution(int seqNum) {
        completeExecution(seqNum, this.defaultWaitTime);
    }

    public void completeExecution(int seqNum, long sleepWaitTime) {
        executionTasks.get(seqNum).completeExecution();
        try {
            Thread.sleep(sleepWaitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Blocks until all task threads have started and are ready to lock
     */
    abstract public void awaitReadyToLock();

    /**
     * @return true if awaitReadyToLock has been called and threads are ready to lock, otherwise will return false
     */
    abstract protected boolean threadsAreReadyToLock();

    /**
     * Create a new instance of an IExecutionController to be passed into
     * the constructor of the Reader or Writer
     *
     * @return a new instance of an IExecutionController
     */
    abstract protected ExecutionController newExecutionController();

    /**
     * A ControlledExecution Writer that requires the writer lock
     *
     * @param <T>
     */
    private class Writer<T> extends ControlledExecution<T> {
        private Writer(int seqNum, ExecutionController controller, Callable<T> callable) {
            super("W" + seqNum, seqNum, controller, callable);
        }

        @Override
        protected void lock() {
            this.lockForWrite();
        }
    }

    /**
     * A ControlledExecution Reader that requires the reader lock
     *
     * @param <T>
     */
    private class Reader<T> extends ControlledExecution<T> {
        private Reader(int seqNum, ExecutionController controller, Callable<T> callable) {
            super("R" + seqNum, seqNum, controller, callable);
        }

        @Override
        protected void lock() {
            this.lockForRead();
        }
    }

}
