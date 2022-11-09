package edu.yu.parallel.execution;

import edu.yu.parallel.RWLockInterface;

public class ExecutionGroup<T> extends AbstractExecutionGroup<T> {

    public ExecutionGroup(RWLockInterface rwLock, long defaultWaitTime) {
        super(rwLock, defaultWaitTime);
    }

    protected ExecutionController newExecutionController() {
        throw new RuntimeException("TODO");
    }

    public void awaitReadyToLock() {
        throw new RuntimeException("TODO");
    }

    protected boolean threadsAreReadyToLock() {
        throw new RuntimeException("TODO");
    }

}
