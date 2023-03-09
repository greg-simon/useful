package au.id.simo.useful;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Used to help ensure resources are cleaned up when required.
 * <p>
 * Defer is conceptually a collection of items that are executed when
 * {@link #execute()} is called:
 * <ul>
 * <li>{@link Runnable}: Has {@code run()} called</li>
 * <li>{@link AutoCloseable}: Has {@code close()} called</li>
 * <li>{@link ExecutorService}: Has {@code shutdownNow()} called</li>
 * </ul>
 * <p>
 * Cleanup tasks are performed in reverse added order (LIFO), consistent with
 * try-with-resources behavior for {@link AutoCloseable} implementations.
 * <p>
 * Defer implements AutoCloseable for convenience to enable it to be used easily
 * in try-with-resources blocks.
 * <p>
 * Example of simple instance usage:
 * <pre>
 * try (Defer defer = new Defer()) {
 *    ExecutorService service = defer.shutdownNow(Executors.newCachedThreadPool());
 *    defer.run(() -&gt; {
 *       System.out.println("This will be printed at the end of the try block");
 *    });
 *    service.execute(...);
 *    ...
 * } // service has shutdownNow() called here
 * </pre>
 * <p>
 * Defer can also be registered with
 * {@link Runtime#addShutdownHook(java.lang.Thread)} for {@link #execute()} to
 * run on JVM shutdown, by simply calling {@link #registerShutdownHook()}. It
 * can also be removed with {@link #unregisterShutdownHook()}.
 * <pre>
 * Defer onShutdown = new Defer().registerShutdownHook();
 * onShutdown.run(...);
 * ...
 * </pre>
 * <p>
 * Items added to the Defer instance are only ever cleaned up once before being
 * discarded. This makes it safe to call an instances {@link #execute()} method
 * multiple times.
 */
public class Defer implements AutoCloseable {

    private static final String SELF_ADD_ERROR_MSG
            = "Infinite loop detected, a Defer can not be added to itself.";

    private static final DeferErrorHandler NO_OP_POLICY = new DeferErrorHandler() {
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

    /**
     * Executed in stack order: LIFO.
     * <p>
     * Could contain any of the following types:
     * <ul>
     * <li>{@link Runnable}</li>
     * <li>{@link AutoCloseable}</li>
     * <li>{@link ExecutorService}</li>
     * </ul>
     */
    private final Deque<Object> itemsToExec;

    /**
     * The handler used to act on any task that throws an exception.
     */
    private DeferErrorHandler handler;

    /**
     * Thread instance that has been registered with
     * {@link Runtime#addShutdownHook(java.lang.Thread)}. Only calling
     * {@link #registerShutdownHook()} will set this variable.
     */
    private Thread shutdownHookThread;

    public Defer() {
        this.itemsToExec = new LinkedBlockingDeque<>();
        this.handler = NO_OP_POLICY;
    }

    /**
     *
     * @return the number of tasks registered to be executed.
     */
    public int size() {
        return itemsToExec.size();
    }

    public void setErrorHandler(DeferErrorHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    /**
     * Executes all tasks that have been registered.
     * <p>
     * Any executed task is removed from the list to ensure one-run-only policy,
     * even if an exception is thrown.
     * <p>
     * Tasks are executed in reverse order they were added in (LIFO), to ensure
     * the behavior matches try-with-resources behavior.
     * <p>
     * Any Exception thrown by any task handled by a {@link DeferErrorHandler}
     * if provided, otherwise the exception is ignored.
     */
    public void execute() {
        // Execute in reverse order.
        while (!itemsToExec.isEmpty()) {
            Object item = itemsToExec.pop();
            if (item instanceof Runnable) {
                execRunnable((Runnable) item);
            } else if (item instanceof AutoCloseable) {
                execClosable((AutoCloseable) item);
            } else if (item instanceof ExecutorService) {
                execExecutorService((ExecutorService) item);
            }
            // unknown item: ignore
        }
    }

    private void execRunnable(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception t) {
            handler.handle(runnable, t);
        }
    }

    private void execClosable(AutoCloseable closable) {
        try {
            closable.close();
        } catch (Exception ex) {
            handler.handle(closable, ex);
        }
    }

    private void execExecutorService(ExecutorService service) {
        try {
            service.shutdownNow();
        } catch (Exception ex) {
            handler.handle(service, ex);
        }
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
     * Registers a {@link Runnable} for later execution.
     *
     * @param <R> The exact type passed as an argument.
     * @param task executed when this instances {@link Defer#execute()}
     * method is run.
     * @return the same runnable instance passed as an argument, to allow this
     * method to be used inline with declaration and assignment.
     */
    public <R extends Runnable> R run(R task) {
        if (task == null) {
            return null;
        }
        itemsToExec.push(task);
        return task;
    }

    /**
     * Registers an ExecutorServices for later shutdown.
     * <p>
     * On {@link Defer#execute()}, {@link ExecutorService#shutdownNow()} will be
     * called and any remaining Runnable tasks will be discarded.
     * <p>
     * Usage example:
     * <pre>
     * ExecutorService service = defer.shutdownNow(Executors.newCachedThreadPool());
     * </pre>
     *
     * @param <S> The exact type passed as an argument.
     * @param service The ExecutorService instance to shut down when this
     * Defers execute method is called.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with ExecutorService declaration.
     */
    public <S extends ExecutorService> S shutdownNow(S service) {
        if (service == null) {
            return null;
        }
        itemsToExec.push(service);
        return service;
    }

    /**
     * Registers an AutoClosable for later closing.
     * <p>
     * Usage example:
     * <pre>
     * try (Defer defer = new Defer()) {
     *    InputStream in = defer.close(new FileInputStream("in.txt"));
     *    OutputStream out = defer.close(new FileInputStream("out.txt"));
     *    ...
     * } // streams closed here, 'out' first then 'in'
     * </pre>
     *
     * @param <C> The exact type passed as an argument.
     * @param closable The AutoClosable instance to close when this instance's
     * execute method is called.
     * @return the same closable instance passed as an argument, to allow this
     * method to be used inline with AutoClosable declaration.
     * @throws IllegalArgumentException if this instance is added to
     * itself via this method, otherwise it would result in an infinite loop when
     * execute() is called, until a stack overflow exception is thrown.
     */
    public <C extends AutoCloseable> C close(C closable) {
        if (closable == null) {
            return null;
        }
        if (closable == this) {
            throw new IllegalArgumentException(SELF_ADD_ERROR_MSG);
        }
        itemsToExec.push(closable);
        return closable;
    }

    /**
     * @return True if this instance is registered as a JVM shutdown hook.
     * @see #registerShutdownHook()
     * @see #unregisterShutdownHook()
     * @see Runtime#addShutdownHook(java.lang.Thread)
     */
    public synchronized boolean isShutdownHookRegistered() {
        return shutdownHookThread != null;
    }

    /**
     * Registers this instance, so it will be executed on JVM shutdown.
     * <p>
     * Calling this method on an already registered instance is safe as it will
     * have no effect.
     *
     * @return The instance of this class (${@code this})
     * @see Runtime#addShutdownHook(java.lang.Thread)
     */
    public synchronized Defer registerShutdownHook() {
        if (shutdownHookThread == null) {
            shutdownHookThread = new Thread(this::execute);
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);
        }
        return this;
    }

    /**
     * Unregisters this Cleaner instance from execution on JVM shutdown.
     * <p>
     * Calling this method on an already unregistered Defer is safe as it will
     * have no effect.
     *
     * @return The instance of this class (${@code this})
     * @see #registerShutdownHook()
     */
    public synchronized Defer unregisterShutdownHook() {
        if (shutdownHookThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
            shutdownHookThread = null;
        }
        return this;
    }
}
