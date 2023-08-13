package au.id.simo.useful.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * ExecutorService that doesn't manage its own threads, instead it relies on
 * the caller to use {@link #runTask()} to run a task.
 * 
 */
public class ManualExecutorService extends AbstractExecutorService {
    
    private volatile boolean shutdown;
    public final BlockingQueue<Runnable> tasks;

    public ManualExecutorService() {
        shutdown = false;
        tasks = new LinkedBlockingQueue<>();
    }
    
    /**
     * Runs a single task if any are present, otherwise it returns having done
     * nothing.
     */
    public void runTask() {
        Runnable task = tasks.poll();
        if (task != null) {
            task.run();
        }
        notifyIfNeeded();
    }

    /**
     * Polls for tasks to run in a loop, as long as the service is not shutdown, or there are still tasks to run.
     * 
     * @param pollWaitInMS the number of milliseconds to wait for a new task before looping and trying again.
     * each loop.
     */
    public void runTaskLoop(long pollWaitInMS) {
        try {
            Runnable task;
            while (!shutdown || !tasks.isEmpty()) {
                task = tasks.poll(pollWaitInMS, TimeUnit.MILLISECONDS);
                if (task == null) {
                    notifyIfNeeded();
                    continue;
                }
                task.run();
            }
        } catch (InterruptedException e) {
            // can occur while tasks.poll is waiting for a task.
            // do nothing
        }
    }
    
    private synchronized void notifyIfNeeded() {
        if (isTerminated()) {
            this.notifyAll();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
        tasks.clear();
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        ArrayList<Runnable> list = new ArrayList<>(tasks.size());
        tasks.drainTo(list);
        return list;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && tasks.isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        shutdown = true;
        long timeoutMillis = unit.toMillis(timeout);
        if (tasks.isEmpty()) {
            // shutdown is true and there are no more tasks. It's terminated.
            return true;
        }
        synchronized (this) {
            this.wait(timeoutMillis);
        }
        return isTerminated();
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("Service is shutdown.");
        }
        tasks.add(command);
    }
}
