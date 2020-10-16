package au.id.simo.useful.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * For use in ExecutorServices to control the names of the threads.
 */
public class NamedThreadFactory implements ThreadFactory {

    public final AtomicInteger threadNum;
    public final String namePrefix;
    public final boolean daemon;

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.threadNum = new AtomicInteger(0);
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    private String getNextThreadName() {
        String nextThreadName = String.format(
                "%s-%02d",
                namePrefix,
                threadNum.getAndIncrement());
        return nextThreadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, getNextThreadName());
        if (thread.isDaemon() != daemon) {
            thread.setDaemon(daemon);
        }
        return thread;
    }
}
