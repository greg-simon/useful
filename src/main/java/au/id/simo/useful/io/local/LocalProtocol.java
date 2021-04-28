package au.id.simo.useful.io.local;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a URL protocol implementation that allows URL access to data from
 * {@link Resource} implementations.
 * <p>
 * The components of the {@code local://} protocol are:
 * <pre>
 * local://[sessionId]/[path]
 * </pre>
 * Example:
 * <pre>
 * local://00001/index.html
 * </pre>
 * <p>
 * Each {@code sessionId} is a five digit incrementing number
 * assigned when the session is created. The {@code path} is provided by the
 * calling code when resources are registered to the session.
 * <p>
 * Usage:
 * <pre>
 * try (URLSession session = LocalProtocol.newSession()) {
 *     String url = session.register("index.html", new File("mypage.html"));
 *
 *     // url is "local://00001/index.html"
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
    private static final Map<String, LocalSession> SESSION_REGISTRY = new HashMap<>();
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
            String sessionId = generateSessionId();
            LocalSession newSession = new LocalSession(sessionId);
            SESSION_REGISTRY.put(sessionId, newSession);
            return newSession;
        }
    }

    /**
     * Generates a String for use as a LocalSession id from the SESSION_COUNTER.
     *
     * @return An incrementing number as a string. It is zero padded to be five
     * characters in length.
     */
    private static String generateSessionId() {
        return String.format("%05d", SESSION_COUNTER.incrementAndGet());
    }

    /**
     * Lookup an existing session with the provided session ID.
     *
     * @param sessionId the session id that uniquely identifies an existing
     * LocalSession instance.
     * @return a LocalSession identified by the sessionId, or null of no such
     * session exists.
     */
    protected static LocalSession getSession(String sessionId) {
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
