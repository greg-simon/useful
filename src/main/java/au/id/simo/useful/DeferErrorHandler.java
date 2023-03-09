package au.id.simo.useful;

import java.util.concurrent.ExecutorService;

/**
 * A handler for Defer tasks that throw an exception when attempting to execute
 * them.
 */
public interface DeferErrorHandler {

    /**
     * Handles any unchecked exceptions from running {@link Runnable#run()} as
     * a deferred task.
     * 
     * @param runnable The Runnable instance that threw the exception.
     * @param exception The exception that was thrown.
     */
    public void handle(Runnable runnable, Exception exception);

    /**
     * Handles any exceptions from running {@link AutoCloseable#close()}.
     *
     * @param closable The AutoCloseable instance that caused the exception.
     * @param exception The exception thrown when attempting to close the
     * AutoClosable.
     */
    public void handle(AutoCloseable closable, Exception exception);
    
    /**
     * Handles any exceptions from running {@link ExecutorService#shutdownNow()}.
     *
     * @param service The ExecutorService instance that caused the exception.
     * @param exception The exception thrown when attempting to shut down the
     * ExecutorService.
     */
    public void handle(ExecutorService service, Exception exception);
}
