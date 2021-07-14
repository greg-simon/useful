package au.id.simo.useful.io.local;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
 * The Integer sessionId means the upper limits for active sessions at the same
 * time is {@link Integer#MAX_VALUE}.
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
     * The registry of all sessions in the current application. Indexed by
     * sessionId.
     */
    private static final Map<Integer, LocalSession> SESSION_REGISTRY = new HashMap<>();
    /**
     * Used to allocate new sessionIds.
     */
    private static final AtomicInteger SESSION_COUNTER = new AtomicInteger();

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
     */
    public static LocalSession newSession() {
        Handler.registerHandlerIfRequired();
        // in a syncblock to make the session 'generate' and 'add'
        // operations into a single atomic operation.
        synchronized (SESSION_REGISTRY) {
            Integer sessionId = generateSessionId();
            LocalSession newSession = new LocalSession(sessionId);
            SESSION_REGISTRY.put(sessionId, newSession);
            return newSession;
        }
    }
    
    /**
     * Allocates an Integer for use as a LocalSession id.
     * 
     * @return An incrementing Integer.
     */
    private static Integer generateSessionId() {
        return SESSION_COUNTER.incrementAndGet();
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
     * Cleans up all resources used for the provided Session.
     *
     * @param session close this session and remove it from the registry.
     */
    protected static void closeSession(LocalSession session) {
        synchronized (SESSION_REGISTRY) {
            LocalSession sess = SESSION_REGISTRY.remove(session.getId());
            if (sess != null) {
                sess.closeLocalSession();
            }
        }
    }
}
