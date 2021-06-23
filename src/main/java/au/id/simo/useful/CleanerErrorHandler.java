package au.id.simo.useful;

/**
 * A handler for cleaner tasks that throw an exception when attempting to clean
 * them up.
 */
public interface CleanerErrorHandler {

    /**
     * Handles any unchecked exceptions from running {@link Runnable#run()} as
     * cleanup tasks.
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
}
