package au.id.simo.useful.io.local;

/**
 * A managed collection of {@link LocalSession}s, it represents a namespace for the {@code local://} protocol.
 * <pre>
 *     local://name-of-local-session-registry-implementation.sessionId/path/to/resource
 * </pre>
 * @see LocalProtocol
 */
public interface LocalSessionRegistry {
    /**
     * @return The namespace that identifies this registry, usable with a local URL.
     */
    String getNamespace();

    /**
     * @return the maximum number of characters that will be required to describe any session ID
     * that could be allocated by this registry.
     */
    int getMaxSessionIdLength();

    /**
     * @return A newly created session that is active and registered.
     */
    LocalSession newSession();

    /**
     * @param sessionId The string representation of the session ID allocated by this registry.
     * @return The session that is identified by the provided hostname, or null if no session with that ID was found,
     *         or also null if the sessionId was invalid in any way.
     */
    LocalSession getSession(String sessionId);

    /**
     * Removes the provided session from the registry.
     * <p>
     * Implementation Notes:
     * <ul>
     * <li>Implementations should ignore any provided sessions that do not already exist in the registry</li>
     * <li>The provided session should not be closed from this method, as the {@link LocalSession#close()} method calls
     * this, and if the session is closed here it will result in a stack overflow</li>
     * </ul>
     * @param session The session to be unregistered.
     */
    void unregisterSession(LocalSession session);

    /**
     * Closes all sessions in this registry.
     * @return the number of sessions that were closed from this method call.
     */
    int closeAllSessions();

    /**
     * @return The number of active sessions this registry can provide.
     */
    int capacity();

    /**
     * @return the number of active sessions currently registered.
     */
    int size();
}
