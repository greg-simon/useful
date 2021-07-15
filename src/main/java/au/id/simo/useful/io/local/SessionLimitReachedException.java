package au.id.simo.useful.io.local;

/**
 * Occurs when {@link LocalProtocol#newSession()} is called and there already
 * exists the maximum number of LocalSessions.
 */
public class SessionLimitReachedException extends RuntimeException {

    public SessionLimitReachedException(String string) {
        super(string);
    }
}
