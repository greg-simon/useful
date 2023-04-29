package au.id.simo.useful;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Helps ensure resources are cleaned up.
 * <p>
 * Defer is conceptually a collection of {@link AutoCloseable}s that are closed
 * when {@link #close()} is called in last in, first out (LIFO) order. Consistent
 * with try-with-resources behavior.
 * <p>
 * Usages:
 * <ul>
 * <li>
 * Make try-with-resources blocks slightly neater when several items are declared:
 * <p>
 * Without Defer
 * <pre>
 * try (Connection conn = DriverManager.getConnection(...);
 *      Statement stmt = conn.createStatement();
 *      ResultSet rs = stmt.executeQuery(QUERY_STR);) {
 *    while (rs.next()) {
 *        System.out.println(rs.getString(1));
 *    }
 * }
 * </pre>
 * <p>
 * Using Defer
 * <pre>
 * try (Defer defer = new Defer()) {
 *    Connection conn = defer.close(DriverManager.getConnection(...));
 *    Statement stmt = defer.close(conn.createStatement());
 *    ResultSet rs = defer.close(stmt.executeQuery(QUERY_STR));
 *    while (rs.next()) {
 *        System.out.println(rs.getString(1));
 *    }
 * }
 * </pre>
 * </li>
 * <li>
 * <p>
 * Easily register {@link AutoCloseable}s to close on JVM shutdown.
 * <pre>
 * Defer onShutdown = new Defer().registerShutdownHook();
 * onShutdown.close(...);
 * </pre>
 * See {@link Runtime#addShutdownHook(java.lang.Thread)} for the underlying
 * mechanism used.
 * </li>
 * </ul>
 * As of Java 19, the {@link ExecutorService} implements {@link AutoCloseable},
 * however previous versions can use the {@link #shutdown(ExecutorService)}
 * method, which back ports the same behavior using an internal wrapper class.
 * <p>
 * Items added to the Defer instance are only ever cleaned up once before being
 * discarded. This makes it safe to call an instances {@link #close()} method
 * multiple times.
 */
public class Defer implements AutoCloseable {

    private static final String SELF_ADD_ERROR_MSG
            = "Infinite loop detected, a Defer can not be added to itself.";

    /**
     * Ignores any exceptions thrown when {@link AutoCloseable#close()} is called on items.
     */
    public static final DeferErrorHandler IGNORE_EXCEPTIONS_POLICY = (closable, exception) -> {
        // no op
    };

    /**
     * Closed in stack order: LIFO.
     */
    private final Deque<AutoCloseable> itemsToClose;

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
        this.itemsToClose = new LinkedBlockingDeque<>();
        this.handler = IGNORE_EXCEPTIONS_POLICY;
    }

    /**
     *
     * @return the number of tasks registered to be executed.
     */
    public int size() {
        return itemsToClose.size();
    }

    public Defer setErrorHandler(DeferErrorHandler handler) {
        this.handler = Objects.requireNonNull(handler);
        return this;
    }

    /**
     * Closes all items that have been registered.
     * <p>
     * Any closed item is removed from the list to ensure exactly-one-close policy,
     * even if an exception is thrown.
     * <p>
     * Tasks are closed in reverse order they were added in (LIFO), to ensure
     * the behavior matches try-with-resources.
     * <p>
     * Any Exception thrown by any item while closing, is handled by a
     * {@link DeferErrorHandler} if provided, which may include throwing
     * unchecked exceptions. The default handler ignores all exceptions.
     */
    @Override
    public void close() {
        // close in reverse order.
        while (!itemsToClose.isEmpty()) {
            AutoCloseable item = itemsToClose.pop();
            try {
                item.close();
            } catch (Exception ex) {
                handler.handle(item, ex);
            }
        }
    }

    /**
     * Registers an ExecutorServices for later shutdown.
     * <p>
     * On {@link Defer#close()},
     * an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. This method waits until all
     * tasks have completed execution and the executor has terminated.
     * <p>
     * Usage example:
     * <pre>
     * ExecutorService service = defer.shutdown(Executors.newCachedThreadPool());
     * </pre>
     *
     * @param <S> The exact type passed as an argument.
     * @param service The ExecutorService instance to shut down when this
     * Defers execute method is called.
     * @return the same service instance passed as an argument, to allow this
     * method to be used inline with ExecutorService declaration.
     */
    public <S extends ExecutorService> S shutdown(S service) {
        if (service == null) {
            return null;
        }
        itemsToClose.push(new ExecutorServiceWrapper(service));
        return service;
    }

    public static abstract class AutoCloseableWrapper<T> implements AutoCloseable {
        private final T wrappedType;

        public AutoCloseableWrapper(T wrappedType) {
            this.wrappedType = wrappedType;
        }

        public T getWrappedType() {
            return wrappedType;
        }
    }

    /**
     * Back ported ExecutorService close method from Java 19.
     */
    private static class ExecutorServiceWrapper extends AutoCloseableWrapper<ExecutorService> {
        public ExecutorServiceWrapper(ExecutorService service) {
            super(service);
        }

        /**
         * Initiates an orderly shutdown in which previously submitted tasks are
         * executed, but no new tasks will be accepted. This method waits until all
         * tasks have completed execution and the executor has terminated.
         *
         * <p> If interrupted while waiting, this method stops all executing tasks as
         * if by invoking {@link ExecutorService#shutdownNow()}. It then continues to wait until all
         * actively executing tasks have completed. Tasks that were awaiting
         * execution are not executed. The interrupt status will be re-asserted
         * before this method returns.
         *
         * <p> If already terminated, invoking this method has no effect.
         *
         * The default implementation invokes {@code shutdown()} and waits for tasks
         * to complete execution with {@code awaitTermination}.
         *
         * @throws SecurityException if a security manager exists and
         *         shutting down this ExecutorService may manipulate
         *         threads that the caller is not permitted to modify
         *         because it does not hold {@link
         *         java.lang.RuntimePermission}{@code ("modifyThread")},
         *         or the security manager's {@code checkAccess} method
         *         denies access.
         * @since 19
         */
        @Override
        public void close() {
            ExecutorService service = getWrappedType();
            boolean terminated = service.isTerminated();
            if (!terminated) {
                service.shutdown();
                boolean interrupted = false;
                while (!terminated) {
                    try {
                        terminated = service.awaitTermination(1L, TimeUnit.DAYS);
                    } catch (InterruptedException e) {
                        if (!interrupted) {
                            service.shutdownNow();
                            interrupted = true;
                        }
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
        itemsToClose.push(closable);
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
            shutdownHookThread = new Thread(() -> {
                try {
                    this.close();
                } catch (Exception e) {
                    // ignore
                }
            });
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
