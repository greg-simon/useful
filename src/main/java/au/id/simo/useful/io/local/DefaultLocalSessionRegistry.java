package au.id.simo.useful.io.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLocalSessionRegistry implements LocalSessionRegistry {

    /**
     * The namespace this registry instance is registered with, in the LocalProtocol class.
     */
    private final String namespace;
    /**
     * Allocate session ids above or equal to this number.
     */
    private final int minSessionId;
    /**
     * Allocate session ids below or equal to this number.
     */
    private final int maxSessionId;
    /**
     * The maximum number of active sessions.
     */
    private final int capacity;
    /**
     * The registry of all sessions in the current application. Indexed by
     * sessionId.
     */
    private final Map<Integer, LocalSession> registryMap;
    /**
     * Used to allocate new sessionIds. Initialised to one less than min, as the
     * allocator increments first then allocates.
     */
    private int sessionCounter;

    public DefaultLocalSessionRegistry(String namespace) {
        this(namespace, 1, 100_000);
    }

    public DefaultLocalSessionRegistry(String namespace, int minSessionId, int maxSessionId) {
        this.namespace = namespace;
        this.minSessionId = minSessionId;
        this.maxSessionId = maxSessionId;
        this.capacity = maxSessionId - minSessionId + 1;
        this.sessionCounter = minSessionId - 1;
        this.registryMap = new HashMap<>();
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public synchronized LocalSession newSession() {
        Integer sessionId = allocateSessionId();
        LocalSession newSession = new LocalSession(this, String.valueOf(sessionId));
        registryMap.put(sessionId, newSession);
        return newSession;
    }

    /**
     * Allocates an Integer for use as a LocalSession id, guaranteed to be
     * unique among already allocated session ids.
     *
     * @return An incrementing Integer.
     * @throws SessionLimitReachedException if the number of active sessions
     * equals {@link #capacity()} when this method is called.
     */
    private int allocateSessionId() {
        int sessionId = nextSessionId();
        int loopCounter = 0;
        while (registryMap.containsKey(sessionId)) {
            sessionId = nextSessionId();

            if (loopCounter == capacity) {
                // by now we have looped through the full rage of the numbers
                // and none are left to allocate
                throw new SessionLimitReachedException(String.format(
                        "Session limit reached: %s already exist",
                        capacity
                ));
            }
            loopCounter++;
        }
        return sessionId;
    }

    private int nextSessionId() {
        sessionCounter++;
        if (sessionCounter > maxSessionId) {
            sessionCounter = minSessionId;
        }
        return sessionCounter;
    }

    @Override
    public synchronized LocalSession getSession(String hostname) {
        Integer intId = parseIntOrNull(getSessionIdOrNull(hostname));
        if (intId == null) {
            return null;
        }
        return registryMap.get(intId);
    }

    @Override
    public synchronized void unregisterSession(LocalSession session) {
        Integer intId = parseIntOrNull(session.getId());
        if (intId == null) {
            throw new IllegalArgumentException("Session not found in registry: "+ session.getId());
        }
        registryMap.remove(intId);
    }

    @Override
    public int closeAllSessions() {
        // no synchronized required as the session.close() will unregister in a
        // synchronized block anyway.
        List<LocalSession> sessions = new ArrayList<>(registryMap.values());
        int returnValue = sessions.size();
        for(LocalSession session: sessions) {
            session.close();
        }
        return returnValue;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public int size() {
        return registryMap.size();
    }

    /**
     * Attempts to parse an Integer from the provided String.
     *
     * @param integerStr A string representation of an Integer.
     * @return An Integer, or null if the string is unable to be parsed.
     */
    protected static Integer parseIntOrNull(String integerStr) {
        try {
            return Integer.parseInt(integerStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
