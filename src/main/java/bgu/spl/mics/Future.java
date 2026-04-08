package bgu.spl.mics;
 
import java.util.concurrent.TimeUnit;
 
public class Future<T> {
 
    private boolean isDone;
    private T result;  
    public Future() {
        isDone = false;
        result = null;
    }
 
    /**
     * Blocks until the result is available and returns it.
     */
    public synchronized  T get() {
            while (!isDone()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
        }
        return result;
    }
 
    /**
     * Resolves the result of this Future object and notifies all waiting threads.
     */
    public synchronized void resolve(T result) {
            if (!isDone()) {  
                this.result = result;  
                this.isDone = true;  
                notifyAll();          
        }
    }
 
    /**
     * Checks if the result has been resolved.
     */
    public synchronized boolean isDone() {
        return isDone;
    }
 
    /**
     * Blocks for a given timeout and returns the result if available, or null otherwise.
     */
    public synchronized  T get(long timeout, TimeUnit unit) {
            long millisTimeout = unit.toMillis(timeout);
            long endTime = System.currentTimeMillis() + millisTimeout;
 
            while (!isDone() && millisTimeout > 0) {
                try {
                    wait(millisTimeout);
                    millisTimeout = endTime - System.currentTimeMillis();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        return result;
    }
}