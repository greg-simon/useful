package au.id.simo.useful;

/**
 * A handler for {@link AutoCloseable} items registered to a {@link Defer},
 * that throw an exception when attempting to close them.
 */
@FunctionalInterface
public interface DeferErrorHandler {

    /**
     * Handles any exceptions from running {@link AutoCloseable#close()}.
     *
     * @param closable The AutoCloseable instance that caused the exception.
     * @param exception The exception thrown when attempting to close the
     * AutoClosable.
     * @throws DeferException implementations of this method should throw this exception to halt the closing
     * of further {@link AutoCloseable}s.
     */
    void handle(AutoCloseable closable, Exception exception) throws DeferException;

    /**
     * Called after all registered {@link AutoCloseable}s are closed.
     * @throws DeferException
     */
    default void throwExceptions() throws DeferException {}
}
