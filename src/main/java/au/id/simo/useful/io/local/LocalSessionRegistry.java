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
     * @param hostname The hostname field of a local URL. E.g. "{@code namespace.sessionID}"
     *                 from the URL {@code local://namespace.sessionID/path/resource}.
     * @return The session that is identified by the provided hostname, or null if no session with that ID was found.
     */
    LocalSession getSession(String hostname);

    /**
     * Parse the session ID from the hostname field in a local URL.
     * <p>
     * For example, using the local URL {@code local://namespace.sessionID/path/resource},
     * this method will be provided the hostname field as an argument "{@code namespace.sessionID}"
     * and should return the sessionID section "{@code sessionID}".
     *
     * @param hostname The hostname field of a local URL.
     * @return The sessionID component within the provided hostname, or null if one could
     * not be found.
     */
    default String getSessionIdOrNull(String hostname) {
        if (hostname == null) {
            return null;
        }
        int dotIdx = hostname.indexOf('.');
        if (dotIdx < 0) {
            // no dot found, we won't be able to identify a sessionId without it.
            return null;
        }
        if (dotIdx == hostname.length() - 1) {
            // last char is the first dot, making the session part zero length.
            return null;
        }
        return hostname.substring(dotIdx + 1);
    }

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
