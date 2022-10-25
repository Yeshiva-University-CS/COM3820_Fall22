package edu.yu.parallel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

class OfficialRWLockTest {
    private final static Logger logger =
            LogManager.getLogger(OfficialRWLockTest.class);

    static {
        Configurator.setLevel("edu.yu.parallel", Level.INFO);
    }

    @BeforeEach
    void setUp() {
    }


    @AfterEach
    void tearDown() {
    }

    @Nested
    @DisplayName("Single-thread basic lock/unlock")
    class SingleThreadLockUnlockUseCases {

        @Test
        @DisplayName("acquire read lock then unlock")
        public void getReadLockThenUnlock() {
            final RWLock lock = new RWLock();
            lock.lockRead();
            lock.unlock();
            lock.lockRead();
            lock.unlock();
        }

        @Test
        @DisplayName("acquire write lock then unlock")
        public void getWriteLockThenUnlock() {
            final RWLock lock = new RWLock();
            lock.lockWrite();
            lock.unlock();
            lock.lockWrite();
            lock.unlock();
        }

        @Test
        @DisplayName("Exception when nobody has a lock")
        public void cantUnlockWhenNobodyHasLock() {
            Assertions.assertThrows(IllegalMonitorStateException.class, () -> {
                final RWLock lock = new RWLock();
                lock.unlock();
            });
        }
    }

    @Nested
    @DisplayName("Multi-thread basic lock/unlock")
    class MultiThreadLockUnlockUseCases {

        @Test
        @DisplayName("multiple threads can own read lock")
        public void multipleThreadsCanHaveAReadLock()  {
            final RWLock lock = new RWLock();
            lock.lockRead();

            var waiting = new AtomicInteger(0);

            Runnable r = () -> {
                waiting.incrementAndGet();
                lock.lockRead();
                waiting.decrementAndGet();
                logger.info("{} owns the read lock", Thread.currentThread().getName());
                lock.unlock();
            };

            var t2 = new Thread(r, "T2");
            var t3 = new Thread(r, "T3");

            t2.start();
            t3.start();

            Assertions.assertDoesNotThrow(() -> {
                t2.join(100);
                t3.join(100);
            });

            Assertions.assertEquals(0, waiting.get());
        }

        @Test
        @DisplayName("cannot lock for read when another thread is writing")
        public void cannotLockForReadWhenLockedForWrite()  {
            final RWLock lock = new RWLock();
            lock.lockWrite();

            var waiting = new AtomicInteger(0);

            Runnable r = () -> {
                waiting.incrementAndGet();
                lock.lockRead();
                waiting.decrementAndGet();
                logger.info("{} owns the read lock", Thread.currentThread().getName());
                lock.unlock();
            };

            var t2 = new Thread(r, "T2");
            var t3 = new Thread(r, "T3");

            t2.start();
            t3.start();

            Assertions.assertDoesNotThrow(() -> {
                t2.join(100);
                t3.join(100);
            });

            Assertions.assertEquals(2, waiting.get());
        }


    }
}