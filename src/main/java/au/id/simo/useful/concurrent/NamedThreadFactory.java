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
    public ThreadGroup group;

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.threadNum = new AtomicInteger(0);
        this.namePrefix = namePrefix;
        ThreadGroup mainGroup = getMainThreadGroup();
        this.group = new ThreadGroup(mainGroup, namePrefix);
        this.group.setDaemon(daemon);
        this.daemon = daemon;
    }

    private ThreadGroup getMainThreadGroup() {
        // find the root group 'system'
        ThreadGroup system = null;
        ThreadGroup threadGroup
                = Thread.currentThread().getThreadGroup();
        while (threadGroup != null) {
            system = threadGroup;
            threadGroup = threadGroup.getParent();
        }
        if (system == null) {
            return null;
        }
        // look in system thread group for an array of groups
        int groupCount = system.activeGroupCount();
        ThreadGroup[] tgArray = new ThreadGroup[groupCount];
        system.enumerate(tgArray, false);

        // look in array for 'main'
        for (ThreadGroup thisGroup : tgArray) {
            String name = thisGroup.getName();
            if ("main".equals(name)) {
                return thisGroup;
            }
        }

        return null;
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
        if (group.isDestroyed()) {
            // this happens when the group has no threads left
            ThreadGroup mainGroup = getMainThreadGroup();
            group = new ThreadGroup(mainGroup, namePrefix);
        }
        Thread thread = new Thread(group, r, getNextThreadName());
        if (thread.isDaemon() != daemon) {
            thread.setDaemon(daemon);
        }
        return thread;
    }
}
