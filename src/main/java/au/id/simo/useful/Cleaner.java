package au.id.simo.useful;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Used to help ensure resources are cleaned up when required.
 * <p>
 * Cleaner is conceptually a collection of {@link Runnable} instances that are
 * executed when {@link #execute() } is called.
 * <p>
 * Any {@link AutoCloseable} added will be executed in reverse order in of
 * addition, with respect to other {@link AutoCloseable}s, consistent with the
 * try-with-resources contract. Other items should not rely on order of
 * execution, as their order is undefined.
 * <p>
 * Cleaner implements Runnable and AutoCloseable for convenience to enable it to
 * be used easily in try-with-resources blocks and
 * {@link Runtime#addShutdownHook(java.lang.Thread) }
 * <p>
 * Example of simple instance usage:
 * <pre>
 * try (Cleaner cleaner = new Cleaner()) {
 *    ExecutorService service = cleaner.executorService(Executors.newCachedThreadPool());
 *    cleaner.add(() -&gt; {
 *       System.out.println("This will be printed at the end of the try block");
 *    });
 *    service.execute(...);
 *    ...
 * }
 * </pre>
 *
 * Cleaner also has two static instances:
 * <ol>
 * <li>{@code onShutdown}: This instance is registered with
 * {@link Runtime#addShutdownHook(java.lang.Thread) } to be executed on JVM
 * shutdown. This allows any stand alone application code to add resources to be
 * cleaned up on shutdown from anywhere.
 *
 * <li>{@code onDemand}: This instance is never executed unless
 * {@link #onDemandClean()} is called. This is a better option for applications
 * requiring more control over when resources are cleaned up, such as within
 * Servlet containers or regularly scheduled cleanups. It's singleton like
 * nature also enables callers to use it from anywhere without the need of
 * passing a Cleaner instance around manually.
 * </ol>
 * <p>
 * {@link Runnable}s added to the Cleaner list are only ever run once before
 * being discarded. This makes repeat calls to an instances {@link #execute() }
 * safe.
 */
public class Cleaner implements AutoCloseable, Runnable, Iterable<Runnable> {

    /**
     * Utility for use in adapting any cleanup function into a
     * try-with-resources block.
     * <p>
     * No item is added to any Cleaner instance.
     * <p>
     * Usage example of clearing a {@link java.util.List}:
     * <pre>
     * try (AutoClosable ac = Cleaner.autoClose(list, (l) -&gt; {l.clear()})) {
     *    ...
     * } // list cleared here even if exception thrown in try block
     * </pre>
     *
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

    private static Cleaner onShutdownInstance;
    private static Cleaner onDemandInstance;

    /**
     * Obtains a Cleaner instance that will be executed on JVM shutdown.
     *
     * @return a Cleaner instance that will be executed on JVM shutdown.
     * @see Runtime#addShutdownHook(java.lang.Thread)
     */
    public static synchronized Cleaner onShutdown() {
        if (onShutdownInstance == null) {
            onShutdownInstance = new Cleaner();
            Runtime.getRuntime().addShutdownHook(new Thread(onShutdownInstance));
        }
        return onShutdownInstance;
    }

    /**
     * Obtains a Cleaner instance that requires {@link Cleaner#onDemandClean()}
     * to be called to be cleaned up.
     * <p>
     * This static instance is best used where the application requires more
     * control over clean up, such as within a Servlet container or regularly
     * scheduled cleanups.
     *
     * @return the on demand Cleaner instance.
     */
    public static synchronized Cleaner onDemand() {
        if (onDemandInstance == null) {
            onDemandInstance = new Cleaner();
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

    /**
     * Cleaned up in added order: FIFO.
     */
    private final List<Runnable> runnables;
    
    /**
     * Cleaned up in stack order: LIFO.
     */
    private final List<AutoCloseable> closables;

    public Cleaner() {
        this.runnables = new ArrayList<>();
        this.closables = new ArrayList<>();
    }

    /**
     * Iterates over all registered {@link Runnable}s.
     *
     * @return An iterator allowing access to all registered {@link Runnable}s
     */
    @Override
    public Iterator<Runnable> iterator() {
        return runnables.iterator();
    }

    /**
     * Runs all the {@link Runnable}s added to the Cleaner list.
     * <p>
     * Any {@link Runnable} that is run is removed from the list to ensure
     * one-run-only policy.
     * <p>
     * Any exception thrown by any {@link Runnable} is ignored.
     */
    public synchronized void execute() {
        // Close closables in reverse order.
        ListIterator<AutoCloseable> closeableItr = closables.listIterator();
        while (closeableItr.hasPrevious()) {
            try {
                closeableItr.previous().close();
            } catch (Throwable t) {
                // ignore and continue
                // TODO: configurable cleaner error handling
            }
        }
        closables.clear();
        
        for (Runnable r : runnables) {
            try {
                r.run();
            } catch (Throwable t) {
                // ignore and continue
                // TODO: configurable cleaner error handling
            }
        }
        runnables.clear();
    }

    /**
     * {@link Runnable#run()} implementation that calls {@link #execute()}.
     */
    @Override
    public void run() {
        execute();
    }

    /**
     * {@link AutoCloseable#close()} implementation that calls
     * {@link #execute()}.
     */
    @Override
    public void close() {
        execute();
    }

    /**
     * Registers a task to be cleaned up.
     *
     * @param cleanupTask executed when this Cleaner instance is executed.
     */
    public void add(Runnable cleanupTask) {
        runnables.add(cleanupTask);
    }

    // specialised helper methods below
    /**
     * Convenience method to make registering ExecutorServices easier.
     * <p>
     * Usage example:
     * <pre>
     * ExecutorService service = cleaner.executorService(Executors.newCachedThreadPool());
     * </pre>
     *
     * @param <S> The exact type passed as an argument.
     * @param service The ExecutorService instance to shutdown when this Cleaner
     * instance is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with ExecutorService declaration.
     */
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

    /**
     * Convenience method to make registering AutoClosable easier.
     * <p>
     * When the Cleanup instance is closed, all AutoClosables added with this
     * method will be closed in reverse order they are added in (LIFO).
     * <p>
     * Usage example:
     * <pre>
     * try (Cleaner clean = new Cleaner()) {
     *    InputStream in = clean.closable(new FileInputStream("in.txt"));
     *    OutputStream out = clean.closable(new FileInputStream("out.txt"));
     *    ...
     * } // streams closed here
     * </pre>
     *
     * @param <C> The exact type passed as an argument.
     * @param closable The AutoClosable instance to shutdown when this Cleaner
     * instance is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with AutoClosable declaration.
     */
    public <C extends AutoCloseable> C closable(C closable) {
        closables.add(closable);
        return closable;
    }
}
