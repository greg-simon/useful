package au.id.simo.useful;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Used to help ensure resources are cleaned up when required.
 * <p>
 * Cleaner is conceptually a collection of the following types that are cleaned
 * up when {@link #clean()} is called:
 * <ul>
 * <li>{@link Runnable}: Has {@code run()} called</li>
 * <li>{@link AutoCloseable}: Has {@code close()} called</li>
 * <li>{@link ExecutorService}: Has {@code shutdownNow()} called</li>
 * </ul>
 * <p>
 * Cleanup tasks are performed in reverse added order (LIFO), consistent with
 * try-with-resources behavior for {@link AutoCloseable} implementations.
 * <p>
 * Cleaner implements AutoCloseable for convenience to enable it to be used
 * easily in try-with-resources blocks.
 * <p>
 * Example of simple instance usage:
 * <pre>
 * try (Cleaner cleaner = new Cleaner()) {
 *   ExecutorService service = cleaner.shutdownLater(Executors.newCachedThreadPool());
 *   cleaner.runLater(() -&gt; {
 *       System.out.println("This will be printed at the end of the try block");
 *    });
 *    service.execute(...);
 *    ...
 * } // service has shutdownNow() called here
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
 * Items added to the Cleaner list are only ever cleaned up once before
 * being discarded. This makes it safe to call an instances {@link #clean()}
 * method multiple times.
 */
public class Cleaner implements AutoCloseable {

    private static final String SELF_ADD_ERROR_MSG = "Infinite loop detected, a Cleaner can not cleanup itself.";
    private static final CleanerErrorHandler NO_OP_POLICY = new CleanerErrorHandler() {
        @Override
        public void handle(Runnable runnable, Exception exception) {
            // no op
        }

        @Override
        public void handle(AutoCloseable closable, Exception exception) {
            // no op
        }
        
        @Override
        public void handle(ExecutorService service, Exception exception) {
            // no op
        }
    };

    private static Thread onShutdownThread;
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
            onShutdownThread = new Thread(onShutdownInstance::clean);
            Runtime.getRuntime().addShutdownHook(onShutdownThread);
        }
        return onShutdownInstance;
    }
    
    /**
     * Unregisters the onShutdown instance and returns it if it exists.
     * <p>
     * Future calls to {@link #onShutdown()} will return and register a
     * different instance.
     * 
     * @return The registered Cleaner instance, or null if it doesn't exist.
     */
    protected static synchronized Cleaner unregisterOnShutdown() {
        if (onShutdownInstance == null) {
            return null;
        }
        Runtime.getRuntime().removeShutdownHook(onShutdownThread);
        Cleaner returnCleaner = onShutdownInstance;
        onShutdownInstance = null;
        return returnCleaner;
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
     * <p>
     * Could contain any of the following types:
     * <ul>
     * <li>{@link Runnable}</li>
     * <li>{@link AutoCloseable}</li>
     * <li>{@link ExecutorService}</li>
     * </ul>
     */
    private final Deque<Object> itemsToClean;
    
    /**
     * The handler used to act on any cleanup task that throws an exception.
     */
    private CleanerErrorHandler handler;

    public Cleaner() {
        this.itemsToClean = new ArrayDeque<>();
        this.handler = NO_OP_POLICY;
    }
    
    /**
     * 
     * @return the number of tasks to run on cleanup.
     */
    public int size() {
        return itemsToClean.size();
    }
    
    public void setErrorHandler(CleanerErrorHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    /**
     * Performs all cleanup tasks that have been registered.
     * <p>
     * Any cleanup task performed is removed from the list to ensure
     * one-run-only policy, even if an exception is thrown.
     * <p>
     * Tasks are performed in reverse order they were added in (LIFO), to ensure
     * the behavior matches try-with-resources behavior.
     * <p>
     * Any Exception thrown by any cleanup task handled by a
     * {@link CleanerErrorHandler} if provided, otherwise the exception is is
     * ignored.
     */
    public synchronized void clean() {
        // Cleanup in reverse order.
        while (!itemsToClean.isEmpty()) {
            Object item = itemsToClean.pop();
            if (item instanceof Runnable) {
                cleanRunnable((Runnable) item);
            } else if (item instanceof AutoCloseable) {
                cleanClosable((AutoCloseable) item);
            } else if (item instanceof ExecutorService) {
                cleanExecutorService((ExecutorService) item);
            }
            // unknown item: ignore
        }
    }
    
    private void cleanRunnable(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception t) {
            handler.handle(runnable, t);
        }
    }
    
    private void cleanClosable(AutoCloseable closable) {
        try {
            closable.close();
        } catch (Exception ex) {
            handler.handle(closable, ex);
        }
    }
    
    private void cleanExecutorService(ExecutorService service) {
        try {
            service.shutdownNow();
        } catch (Exception ex) {
            handler.handle(service, ex);
        }
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
    public <R extends Runnable> R runLater(R cleanupTask) {
        if (cleanupTask == null) {
            return null;
        }
        itemsToClean.push(cleanupTask);
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
     * ExecutorService service = cleaner.shutdownLater(Executors.newCachedThreadPool());
     * </pre>
     *
     * @param <S> The exact type passed as an argument.
     * @param service The ExecutorService instance to shutdown when this
     * Cleaners clean method is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with ExecutorService declaration.
     */
    public <S extends ExecutorService> S shutdownLater(S service) {
        if (service == null) {
            return null;
        }
        itemsToClean.push(service);
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
     *    InputStream in = cleaner.closeLater(new FileInputStream("in.txt"));
     *    OutputStream out = cleaner.closeLater(new FileInputStream("out.txt"));
     *    ...
     * } // streams closed here, 'out' first then 'in'
     * </pre>
     *
     * @param <C> The exact type passed as an argument.
     * @param closable The AutoClosable instance to shutdown when this Cleaner
     * instances clean method is executed.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with AutoClosable declaration.
     * @throws IllegalArgumentException if this Cleaner instance is added to
     * itself via this method, otherwise it would result in an infinite loop on
     * cleanup until a stack overflow exception is thrown.
     */
    public <C extends AutoCloseable> C closeLater(C closable) {
        if (closable == null) {
            return null;
        }
        if (closable == this) {
            throw new IllegalArgumentException(SELF_ADD_ERROR_MSG);
        }
        itemsToClean.push(closable);
        return closable;
    }
}