package au.id.simo.useful.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * Used to shutdown any ExecutorService on process exit.
 * <p>
 * A hard process death will prevent this from running.
 * <p>
 * Usage:
 * <pre><code>
 * ExecutorService service = Executors.newCachedThreadPool();
 * Runtime.getRuntime().addShutdownHook(new ShutdownExecutorHook(service));
 * </code></pre>
 *
 */
public class ShutdownExecutorHook extends Thread {

    private final ExecutorService service;

    public ShutdownExecutorHook(ExecutorService service) {
        this.service = service;
    }

    @Override
    public void run() {
        if (service == null) {
            return;
        }
        service.shutdown();
    }

    public static void registerService(ExecutorService service) {
        Runtime.getRuntime().addShutdownHook(
                new ShutdownExecutorHook(service));
    }
}
