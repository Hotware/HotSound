package de.hotware.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class to pause and unpause threads
 * with Java Concurrency
 * @author Martin Braun
 */
public class Pause {

    private Lock mLock;
    private Condition mCondition;
    private AtomicBoolean mPaused;

    public Pause() {
        this.mLock = new ReentrantLock();
        this.mCondition = this.mLock.newCondition();
        this.mPaused = new AtomicBoolean(false);
    }

    /**
     * waits if paused until pause(false) has
     * been called
     * @throws InterruptedException
     */
    public void probe() throws InterruptedException {
        while(this.mPaused.get()) {
            this.mLock.lock();
            try {
                this.mCondition.await();
            } finally {
                this.mLock.unlock();
            }
        }
    }

    /**
     * pauses or unpauses
     */
    public void pause(boolean pValue) {
        if(!pValue){
            this.mLock.lock();
            try {
                this.mCondition.signalAll();
            } finally {
                this.mLock.unlock();
            }
        }
        this.mPaused.set(pValue);
    }
    
    public boolean isPaused() {
    	return this.mPaused.get();
    }

}