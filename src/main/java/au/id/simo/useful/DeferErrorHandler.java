package au.id.simo.useful;

/**
 * A handler for {@link AutoCloseable} items registered to a {@link Defer},
 * that throw an exception when attempting to close them.
 */
public interface DeferErrorHandler {

    /**
     * Handles any exceptions from running {@link AutoCloseable#close()}.
     *
     * @param closable The AutoCloseable instance that caused the exception.
     * @param exception The exception thrown when attempting to close the
     * AutoClosable.
     */
    void handle(AutoCloseable closable, Exception exception);
}
