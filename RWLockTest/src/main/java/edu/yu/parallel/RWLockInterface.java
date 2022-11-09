package edu.yu.parallel;

/* Defines a "read/write lock" interface.  It allows multiple threads to lock
 * in read mode concurrently, but only one thread is allowed to lock in write
 * mode concurrently.
 *
 * Motivation: multiple threads can read from a shared resource without causing
 * concurrency errors.  Concurrency errors only occur when either reads and
 * writes or if multiple writes take place concurrently.
 *
 * The locking rules are as follows.  A thread invoking lockRead() is granted
 * the lock iff no other thread has currently locked the resource for writing.
 * A thread invoking lockWrite() is granted the lock iff no other thread
 * currently has acquired the lock (in either write or read mode)
 *
 *
 */

public interface RWLockInterface {

    /**
     * Acquires the lock iff:
     * 1) No other thread is writing, AND
     * 2) No other threads have requested write access
     * Otherwise, the invoking thread is blocked until the writing thread AND
     * previously blocked threads requesting write access have released the lock
     * <p>
     * NOTE: blocking threads are queued in the order that they requested the lock
     */
    void lockRead();

    /**
     * Acquires the lock iff no other thread currently has acquired the lock in
     * either read or write mode.  Otherwise, the invoking thread is blocked until
     * all previous threads have released the lock.
     * <p>
     * NOTE: blocking threads are queued in the order that they requested the lock
     */
    void lockWrite();

    /**
     * Releases the lock if currently owned by the invoking thread.
     *
     * @throws IllegalMonitorStateException if invoking thread doesn't currently own
     *                                      the lock.
     */
    void unlock();

} // interface