package au.id.simo.useful.io.local;

import java.util.HashMap;
import java.util.Map;

import au.id.simo.useful.io.Resource;

/**
 * Provides a URL protocol implementation that allows URL access to data from
 * {@link Resource} implementations via creating {@link LocalSession}s.
 * <p>
 * The components of the {@code local://} protocol are:
 * <pre>
 * local://[sessionId]/[path]
 * </pre>
 * Example:
 * <pre>
 * local://12345/index.html
 * </pre>
 * <p>
 * Each {@code sessionId} is an Integer assigned when the session is created.
 * The {@code path} is provided by the calling code when resources are
 * registered to the session.
 * <p>
 * Sessions are limited to 100,000 active at any one time via the following
 * constants: {@link #MAX_SESSION_ID}, {@link #MIN_SESSION_ID}. Attempting to
 * create a new session after this limit will throw a
 * {@link SessionLimitReachedException}
 * <p>
 * Usage:
 * <pre>
 * try (URLSession session = LocalProtocol.newSession()) {
 *     String url = session.register("index.html", new File("mypage.html"));
 *
 *     // url is "local://1/index.html"
 *     URL indexUrl = new URL(url);
 *     // code using indexUrl here
 * }
 * </pre>
 * <p>
 * This is particularly useful in rendering web pages using libraries that are
 * based on fetching resources from URLs. As HTML often contains other resources
 * with relative URLs that also need fetching.
 * <p>
 * The local protocol is registered with {@link java.net.URL} when
 * {@code newSession()} is called.
 */
public class LocalProtocol {

    /**
     * Allocate session ids above or equal to this number.
     */
    protected static final int MIN_SESSION_ID = 0;
    /**
     * Allocate session ids below or equal to this number.
     */
    protected static final int MAX_SESSION_ID = 100_000;
    /**
     * The registry of all sessions in the current application. Indexed by
     * sessionId.
     */
    private static final Map<Integer, LocalSession> SESSION_REGISTRY = new HashMap<>();
    /**
     * Used to allocate new sessionIds.
     */
    private static int sessionCounter = MIN_SESSION_ID;

    /**
     * Utility class should not have a public default constructor.
     */
    private LocalProtocol() {
        // no-op
    }

    /**
     * Create a new LocalSession with a unique session id.
     * <p>
     * The local protocol is also registered on the URL package search path if
     * required.
     *
     * @return a new LocalSession.
     * @see Handler#registerHandlerIfRequired()
     * @throws SessionLimitReachedException if
     * ({@link #MAX_SESSION_ID} - {@link #MIN_SESSION_ID}) active sessions
     * already exist when this method is called.
     */
    public static LocalSession newSession() {
        Handler.registerHandlerIfRequired();
        // in a syncblock to make the session 'generate' and 'add'
        // operations into a single atomic operation.
        synchronized (SESSION_REGISTRY) {
            Integer sessionId = allocateSessionId();
            LocalSession newSession = new LocalSession(sessionId);
            SESSION_REGISTRY.put(sessionId, newSession);
            return newSession;
        }
    }
    
    /**
     * Allocates an Integer for use as a LocalSession id, guaranteed to be
     * unique among already allocated session ids.
     * 
     * @return An incrementing Integer.
     * @throws SessionLimitReachedException if
     * ({@link #MAX_SESSION_ID} - {@link #MIN_SESSION_ID}) active sessions
     * already exist when this method is called.
     */
    private static Integer allocateSessionId() {
        int sessionId;
        // save starting value to detect a complete search for an unallocated id
        int loopTerminator = sessionCounter;
        do {
            sessionCounter++;
            if (sessionCounter >= MAX_SESSION_ID) {
                sessionCounter = MIN_SESSION_ID;
            }
            sessionId = sessionCounter;
            if (sessionId == loopTerminator) {
                // by now we have looped through the full rage of the numbers
                // and none are left to allocate
                throw new SessionLimitReachedException(String.format(
                        "Session limit reached: %s already exist",
                        (MAX_SESSION_ID - MIN_SESSION_ID)
                ));
            }
        } while (SESSION_REGISTRY.containsKey(sessionCounter));
        return sessionId;
    }
    
    /**
     * Attempts to parse an Integer from the provided String.
     *
     * @param integerStr A string representation of an Integer.
     * @return An Integer, or null of the string is not able to be parsed.
     */
    protected static Integer parseIntOrNull(String integerStr) {
        try {
            return Integer.parseInt(integerStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Lookup an existing session with the provided session ID.
     *
     * @param sessionId the session id that uniquely identifies an existing
     * LocalSession instance.
     * @return a LocalSession identified by the sessionId, or null of no such
     * session exists.
     */
    protected static LocalSession getSession(Integer sessionId) {
        if (sessionId == null) {
            return null;
        }
        synchronized (SESSION_REGISTRY) {
            return SESSION_REGISTRY.get(sessionId);
        }
    }

    /**
     * Removes the provided Session from the Session register.
     *
     * @param session removes this session from the registry.
     */
    protected static void unregisterSession(LocalSession session) {
        synchronized (SESSION_REGISTRY) {
            SESSION_REGISTRY.remove(session.getId());
        }
    }
}
