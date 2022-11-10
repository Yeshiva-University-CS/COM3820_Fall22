package edu.yu.parallel;

import edu.yu.parallel.execution.ControlledExecution;
import edu.yu.parallel.execution.ExecutionGroup;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OfficialRWLockTest {
    private final static Logger logger = LogManager.getLogger(OfficialRWLockTest.class);
    private static final long STD_WAIT_TIME = 100L;

    static {
        Configurator.setLevel("edu.yu.parallel", Level.INFO);
    }

    private ExecutorService executor;
    private RWLockInterface rwLock;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(10);
        rwLock = new RWLock();
    }


    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Nested
    @DisplayName("Basic lock/unlock use cases")
            //@Timeout(value = 1100, unit = TimeUnit.MILLISECONDS)
    class BasicLockUnlockUseCases {

        @Test
        @DisplayName("Unlock works when I have the read lock")
        public void unlockWorksWhenIHaveTheReadLock() throws InterruptedException {
            final var lock = new RWLock();
            lock.lockRead();
            lock.unlock();
        }

        @Test
        @DisplayName("Unlock works when I have the write lock")
        public void unlockWorksWhenIHaveTheWriteLock() throws InterruptedException {
            rwLock.lockWrite();
            rwLock.unlock();
        }

        @Test
        @DisplayName("Can't unlock w/o lock, when no one has the lock")
        public void cantUnlockWhenNobodyHasLock() {
            Assertions.assertThrows(IllegalMonitorStateException.class, () -> {
                rwLock.unlock();
            });
        }

        @Test
        @DisplayName("Can't unlock w/o lock, when another has the write lock")
        public void cantUnlockWhenSomeoneElseHasWriteLock() throws ExecutionException, InterruptedException {
            rwLock.lockWrite();

            executor.submit(() -> {
                Assertions.assertThrows(IllegalMonitorStateException.class, () -> {
                    rwLock.unlock();
                });
                return null;
            }).get();
        }

        @Test
        @DisplayName("Can't unlock w/o lock, when another has the read lock")
        public void cantUnlockWhenSomeoneElseHasReadLock() throws ExecutionException, InterruptedException {
            rwLock.lockRead();

            executor.submit(() -> {
                Assertions.assertThrows(IllegalMonitorStateException.class, () -> {
                    rwLock.unlock();
                });
                return null;
            }).get();
        }

    }

    @Nested
    @DisplayName("MT locked for write scenarios")
    class MultiThreadLockedForWriteScenarios {
        /**
         * Verifies that write lock blocks subsequent write requests.
         * AND that queued write threads are woken up one by one after the write lock is released
         */
        @Test
        @DisplayName("WWW - Write blocks subsequent writes from getting the lock")
        public void writeLockBlocksWriteRequests() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createWriterTask());
            executor.submit(group.createWriterTask());
            executor.submit(group.createWriterTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Test that subsequent writes wait
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            // Test that waiters writers are woken up and get the lock in order
            group.completeExecution(0);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            group.completeExecution(1);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(2));
        }

        /**
         * Verifies that write lock blocks subsequent reads,
         * AND that queued readers threads are *all* woken up after the write lock is released
         */
        @Test
        @DisplayName("WRR - Write blocks subsequent reads")
        public void writeBlocksReadRequests() throws InterruptedException, ExecutionException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createWriterTask());
            executor.submit(group.createReaderTask());
            executor.submit(group.createReaderTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Test that reads are blocked
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            // Test that reads are both woken up when writer is unlocked
            group.completeExecution(0);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(2));
        }

        /**
         * Verifies that a waiting write request is queued after a previously waiting read request
         */
        @Test
        @DisplayName("WRW - Pending write request obtains the lock after previous pending read request")
        public void writeReadWrite() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createWriterTask());
            executor.submit(group.createReaderTask());
            executor.submit(group.createWriterTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify the initial state
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            // Test that reads are both woken up when writer is unlocked
            group.completeExecution(0);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));
        }

        /**
         * Verifies that a read request is queued after the last queued write request
         * and does not try to leap over it and join a previous pending read group.
         */
        @Test
        @DisplayName("WRWR - Pending read is queued after the last pending write")
        public void writeReadWriteRead() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createWriterTask());
            executor.submit(group.createReaderTask());
            executor.submit(group.createWriterTask());
            executor.submit(group.createReaderTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify the initial state
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(3));

            // Test that the second read is still waiting after the pending reads and writes got the lock
            group.completeExecution(0);
            group.completeExecution(1);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(2));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(3));
        }
    }

    @Nested
    @DisplayName("MT locked for read scenario")
    class MultiThreadLockedForReadScenarios {
        /**
         * Verifies that a read lock blocks a subsequent write request from being able to lock
         * AND write request is automatically woken up and receives the lock when the read releases its lock
         */
        @Test
        @DisplayName("RW - Read lock blocks write from getting lock")
        public void readLockBlocksWriteRequest() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createReaderTask());
            executor.submit(group.createWriterTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify the initial state
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));

            group.completeExecution(0);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
        }

        /**
         * Verifies that multiple threads can read concurrently
         * AND can unlock one without the other
         */
        @Test
        @DisplayName("RRR - Multiple threads can read concurrently")
        public void multipleThreadsCanReadConcurrently() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createReaderTask());
            executor.submit(group.createReaderTask());
            executor.submit(group.createReaderTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify that threads can lock concurrently
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(2));

            // Verify that each thread can be unlocked individually
            group.completeExecution(0);
            group.completeExecution(2);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(2));
        }

        /**
         * Verifies that a queued write request will not get the lock until *all* concurrent reader locks are released
         * AND that the queued writer thread is woken up automatically after the read locks are released
         */
        @Test
        @DisplayName("RRW - Write can obtain lock only after all readers complete")
        public void lockForWriteOnlyAfterAllReadersComplete() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createReaderTask());
            executor.submit(group.createReaderTask());
            executor.submit(group.createWriterTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify that threads can lock concurrently
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            // Verify that the write thread will not get the lock until all readers are done
            group.completeExecution(0);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(2));

            group.completeExecution(1);
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.UNLOCKED, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(2));
        }

        /**
         * Verifies that a pending write blocks a read request, even if there are other
         * readers that currently have the lock
         */
        @Test
        @DisplayName("RWR - Pending write blocks a read")
        public void pendingWriteBlocksRead() throws InterruptedException {
            var group = new ExecutionGroup(rwLock, STD_WAIT_TIME);

            executor.submit(group.createReaderTask());
            executor.submit(group.createWriterTask());
            executor.submit(group.createReaderTask());

            group.awaitReadyToLock();
            group.lockInOrder();

            // Verify the second read is not given the lock along with the current read
            assertEquals(ControlledExecution.LockStatus.LOCKED, group.getLockStatus(0));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
            assertEquals(ControlledExecution.LockStatus.WAITING, group.getLockStatus(1));
        }
    }
}