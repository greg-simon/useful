package au.id.simo.useful;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Used to help ensure resources are cleaned up when required.
 * <p>
 * Cleaner is conceptually a collection of {@link Runnable} instances that are
 * executed when {@link #clean()} is called.
 * <p>
 * Cleanup tasks are performed in reverse added order (LIFO), consistent with
 * try-with-resources behavior for {@link AutoCloseable}s.
 * <p>
 * Cleaner implements Runnable and AutoCloseable for convenience to enable it to
 * be used easily in try-with-resources blocks and
 * {@link Runtime#addShutdownHook(java.lang.Thread) }
 * <p>
 * Example of simple instance usage:
 * <pre>
 * try (Cleaner cleaner = new Cleaner()) {
 *    ExecutorService service = cleaner.shutdown(Executors.newCachedThreadPool());
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
 * <li>{@link #onShutdown()}: This instance is registered with
 * {@link Runtime#addShutdownHook(java.lang.Thread) } to be executed on JVM
 * shutdown. This allows any stand alone application code to add resources to be
 * cleaned up on shutdown from anywhere.
 *
 * <li>{@link #onDemand()}: This instance is never executed unless
 * {@link #onDemandClean()} is called. This is a better option for applications
 * requiring more control over when resources are cleaned up, such as within
 * Servlet containers or regularly scheduled cleanups. It's singleton like
 * nature also enables callers to use it from anywhere without the need of
 * passing a Cleaner instance around manually.
 * </ol>
 * <p>
 * {@link Runnable}s added to the Cleaner list are only ever run once before
 * being discarded. This makes repeat calls to an instances {@link #clean()}
 * safe.
 */
public class Cleaner implements AutoCloseable, Runnable {

    private static final CleanerErrorHandler NO_OP_POLICY = new CleanerErrorHandler() {
        @Override
        public void handle(Runnable runnable, Throwable throwable) {
            // no op
        }

        @Override
        public void handle(AutoCloseable runnable, Throwable throwable) {
            // no op
        }
    };

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
     * Executes {@link #clean()} for the onDemand instance. Cleaning all
     * resources registered with it.
     */
    public static void onDemandClean() {
        onDemand().clean();
    }

    /**
     * Cleaned up in stack order: LIFO.
     */
    private final Deque<Runnable> runnables;
    
    /**
     * The handler used to act on any cleanup task that throws an exception.
     */
    private CleanerErrorHandler handler;

    public Cleaner() {
        this.runnables = new ArrayDeque<>();
        this.handler = NO_OP_POLICY;
    }
    
    /**
     * 
     * @return the number of tasks to run on cleanup.
     */
    public int size() {
        return runnables.size();
    }
    
    public synchronized void setErrorHandler(CleanerErrorHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    /**
     * Performs all cleanup tasks that have been registered.
     * <p>
     * Any cleanup task performed is removed from the list to ensure
     * one-run-only policy.
     * <p>
     * Tasks are performed in reverse order they were added in (LIFO), to ensure
     * the behavior matches try-with-resources behavior.
     * <p>
     * Any exception thrown by any cleanup task is ignored.
     */
    public synchronized void clean() {
        // Cleanup in reverse order.
        while (!runnables.isEmpty()) {
            Runnable runnable = runnables.pop();
            try {
                runnable.run();
            } catch (Throwable t) {
                handler.handle(runnable, t);
            }
        }
    }

    /**
     * {@link Runnable#run()} implementation that calls {@link #clean()}.
     */
    @Override
    public void run() {
        clean();
    }

    /**
     * {@link AutoCloseable#close()} implementation that calls
     * {@link #clean()}.
     */
    @Override
    public void close() {
        clean();
    }

    /**
     * Registers a {@link Runnable} for later running.
     *
     * @param <R> The exact type passed as an argument.
     * @param cleanupTask executed when this instances {@link Cleaner#clean()}
     * method is run.
     * @return the same runnable instance passed as an argument, to allow this
     * method to be used inline with declaration and assignment.
     */
    public <R extends Runnable> R exec(R cleanupTask) {
        if (cleanupTask == null) {
            return null;
        }
        runnables.push(cleanupTask);
        return cleanupTask;
    }

    /**
     * Registers an ExecutorServices for later shutdown.
     * <p>
     * On cleanup, {@link ExecutorService#shutdownNow()} will be called and any
     * remaining Runnable tasks will be discarded.
     * <p>
     * Usage example:
     * <pre>
     * ExecutorService service = cleaner.shutdown(Executors.newCachedThreadPool());
     * </pre>
     *
     * @param <S> The exact type passed as an argument.
     * @param service The ExecutorService instance to shutdown when this
     * Cleaners clean method is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with ExecutorService declaration.
     */
    public <S extends ExecutorService> S shutdown(S service) {
        if (service == null) {
            return null;
        }
        
        runnables.push(() -> {
            service.shutdownNow();
        });
        return service;
    }

    /**
     * Registers an AutoClosable for later closing.
     * <p>
     * When the Cleaner instance is closed, all AutoClosables added with this
     * method will be closed in reverse order they are added in (LIFO),
     * consistent with try-with-resources behavior.
     * <p>
     * Usage example:
     * <pre>
     * try (Cleaner cleaner = new Cleaner()) {
     *    InputStream in = cleaner.close(new FileInputStream("in.txt"));
     *    OutputStream out = cleaner.close(new FileInputStream("out.txt"));
     *    ...
     * } // streams closed here, 'out' first then 'in'
     * </pre>
     *
     * @param <C> The exact type passed as an argument.
     * @param closable The AutoClosable instance to shutdown when this Cleaner
     * instances clean method is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with AutoClosable declaration.
     */
    public <C extends AutoCloseable> C close(C closable) {
        if (closable == null) {
            return null;
        }
        runnables.push(() -> {
            try {
                closable.close();
            } catch (Throwable t) {
                handler.handle(closable, t);
            }
        });
        return closable;
    }
}