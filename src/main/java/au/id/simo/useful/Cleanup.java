package au.id.simo.useful;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Used to help ensure resources are cleaned up when required.
 * <p>
 * Cleanup is conceptually a list of {@link Runnable} instances that are
 * executed in order when {@link #execute() } is called.
 * <p>
 * Cleanup implements Runnable and AutoCloseable for convenience to enable it
 * to be used easily in try-with-resources blocks and
 * {@link Runtime#addShutdownHook(java.lang.Thread) }
 * <p>
 * Example of simple instance usage:
 * <pre>
 * try (Cleanup cleanup = new Cleanup()) {
 *    ExecutorService service = cleanup.executorService(Executors.newCachedThreadPool());
 *    cleanup.add(() -> {
 *       System.out.println("This will be printed at the end of the try block");
 *    });
 *    service.execute(...);
 *    ...
 * }
 * </pre>
 * 
 * Cleanup also has two static instances:
 * <ol>
 * <li>onShutdown: This instance is registered with
 * {@link Runtime#addShutdownHook(java.lang.Thread) } to be executed on JVM
 * shutdown. This allows any stand alone application code to add resources to
 * be cleaned up on shutdown from anywhere.</li>
 * <li>onDemand: This instance is never executed unless {@link #onDemandClean()}
 *  is called. This is a better option for more control over when resources are
 * cleaned up, such as within Servlet containers or to enable regularly
 * scheduled cleanups. It also enables callers to use it from anywhere without
 * the need of passing a Cleanup instance around manually.</li>
 * </ol>
 * {@link Runnable}s added to the Cleanup list are only ever run once before being
 * discarded. Making repeat calls to an instances {@link #execute() } safe.
 */
public class Cleanup implements AutoCloseable, Runnable, Iterable<Runnable> {
    
    /**
     * For use in adapting any cleanup function into a try-with-resources block.
     * @param <T> The object type to be passed to the close Consumer
     * @param obj The object instance to be passed to the close Consumer
     * @param closeOp The closing function
     * @return an instance of AutoClosable for use in a try-with-resources block
     */
    public static <T> AutoCloseable autoClose(T obj, Consumer<T> closeOp) {
        return () -> {
            closeOp.accept(obj);
        };
    }
    
    private static Cleanup onShutdownInstance;
    private static Cleanup onDemandInstance;
    
    /**
     * Used to obtain a Cleanup instance that will be executed on JVM shutdown.
     * @return a Cleanup instance that will be executed on JVM shutdown.
     * @see Runtime#addShutdownHook(java.lang.Thread)
     */
    public static synchronized Cleanup onShutdown() {
        if (onShutdownInstance == null) {
            onShutdownInstance = new Cleanup();
            Runtime.getRuntime().addShutdownHook(new Thread(onShutdownInstance));
        }
        return onShutdownInstance;
    }
    
    /**
     * Used to obtain a Cleanup instance that will no be cleaned up until the
     * following code is run {@link Cleanup#onDemandClean()}.
     * <p>
     * This static instance is best used where the application has more control
     * over clean up, such as within a Servlet container or regularly scheduled
     * cleanups.
     * 
     * @return the on demand Cleanup instance.
     */
    public static synchronized Cleanup onDemand() {
        if (onDemandInstance == null) {
            onDemandInstance = new Cleanup();
        }
        return onDemandInstance;
    }
    
    /**
     * Executes the onDemand instance. Cleaning all resources registered with
     * it.
     */
    public static void onDemandClean() {
        onDemand().execute();
    }
    
    private final List<Runnable> runnables;
    
    public Cleanup() {
        this.runnables = new ArrayList<>();
    }

    @Override
    public Iterator<Runnable> iterator() {
        return runnables.iterator();
    }
    
    /**
     * Runs all the Runnables added to the cleanup list.
     * Any Runnable that is run is removed from the list to ensure one-run-only
     * policy.
     * <p>
     * Any exception thrown by any Runnable is ignored.
     */
    public synchronized void execute() {
        for(Runnable r: runnables) {
            try {
                r.run();
            } catch (Throwable t) {
                // ignore and continue
                // TODO: configurable cleanup error handling
            }
        }
        runnables.clear();
    }
    
    @Override
    public void run() {
        execute();
    }

    @Override
    public void close() throws Exception {
        execute();
    }
    
    
    public void add(Runnable cleanupTask) {
        runnables.add(cleanupTask);
    }
    
    // specialised helper methods below
    
    public <S extends ExecutorService> S executorService(S service) {
        runnables.add(() -> {
            if (service == null) {
                return;
            }
            try {
                service.shutdownNow();
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                // ignore
            }
        });
        return service;
    }
    
    public <C extends AutoCloseable> C closable(C closable) {
        runnables.add(() -> {
            if (closable == null) {
                return;
            }
            try {
                closable.close();
            } catch (Exception ex) {
                // do nothing
            }
        });
        return closable;
    }
}
