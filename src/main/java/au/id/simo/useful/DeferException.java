package au.id.simo.useful;

import java.util.List;

/**
 * Thrown by {@link DeferErrorHandler} implementations, this exception is used to carry one or more exceptions thrown
 * by {@link AutoCloseable#close()}.
 */
public class DeferException extends RuntimeException {
    private final transient List<AutoCloseable> closableList;
    private final List<Exception> exceptionList;

    public DeferException(List<AutoCloseable> closableList, List<Exception> exceptionList, String message, Exception causedBy) {
        super(message, causedBy);
        this.closableList = closableList;
        this.exceptionList = exceptionList;
    }

    public List<AutoCloseable> getClosableList() {
        return closableList;
    }

    public List<Exception> getExceptionList() {
        return exceptionList;
    }
}
