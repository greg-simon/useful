package au.id.simo.useful.io.local;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import au.id.simo.useful.io.Resource;

/**
 * Provides a URL protocol implementation that allows URL access to data from
 * {@link Resource} implementations via creating {@link LocalSession}s.
 * <p>
 * The components of the {@code local://} protocol are:
 * <pre>
 * local://[registryName].[sessionId]/[path]
 * </pre>
 * Example:
 * <pre>
 * local://default.12345/index.html
 * </pre>
 * <p>
 * Each {@code sessionId} is a registry unique String identifier, assigned on
 * new {@link LocalSession} creation. Exactly how this is done is
 * {@link LocalSessionRegistry} implementation specific.
 * The {@code path} is provided by the calling code when resources are
 * registered to the session.
 * <p>
 * There is always a default {@link LocalSessionRegistry} registered under the
 * {@code default} namespace. Which is what the no-argument
 * <p>
 * Usage:
 * <pre>
 * try (URLSession session = LocalProtocol.newSession()) {
 *     String url = session.register("index.html", new File("mypage.html"));
 *
 *     // url is "local://default.1/index.html"
 *     URL indexUrl = URI.create(url).toURL();
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
    private static final String DEFAULT_NAMESPACE = "default";

    private static final Map<String, LocalSessionRegistry> REGISTRY_MAP = new ConcurrentHashMap<>();

    /**
     * Utility class should not have a public default constructor.
     */
    private LocalProtocol() {
        // no-op
    }

    protected static void validateRegistryName(String name) throws IllformedLocaleException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Registry name must be a non-null, non-zero length String.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Registry name must be less than 255 characters in length.");
        }
        boolean valid;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            valid = ((c >= 'a') && (c <= 'z')) ||
                    ((c >= 'A') && (c <= 'Z')) ||
                    ((c >= '0') && (c <= '9')) ||
                    c == '-' || c == '_';
            if (!valid) {
                throw new IllegalArgumentException(String.format("Illegal registry name character: '%s'", c));
            }
        }
    }

    /**
     * When a registry is already registered under the given namespace, it will be
     * replaced, and all existing session closed.
     * @param namespace the namespace to identify the given registry with.
     * @param registry the registry responsible for creating new sessions
     *                 under the provided namespace.
     */
    public void register(String namespace, LocalSessionRegistry registry) {
        validateRegistryName(namespace);
        REGISTRY_MAP.compute(namespace, (k,v) -> {
            if (v !=null) {
                v.closeAllSessions();
            }
            return registry;
        });
    }

    /**
     * Closes all active sessions.
     *
     * @return the number of sessions that were closed.
     */
    public static long closeAllSessions() {
        AtomicLong counter = new AtomicLong();
        REGISTRY_MAP.forEach((k,v) -> {
            counter.addAndGet(v.closeAllSessions());
        });
        return counter.longValue();
    }

    /**
     * Create a new LocalSession from the default {@link LocalSessionRegistry}.
     * <p>
     * The local protocol is also registered on the URL package search path if
     * it's not already.
     * @return a new LocalSession.
     * @see Handler#registerHandlerIfRequired()
     * @throws SessionLimitReachedException if the number of active sessions
     * are exceeded for the {@link LocalSessionRegistry} implementation.
     */
    public static LocalSession newSession() {
        return newSession(DEFAULT_NAMESPACE);
    }

    /**
     * Create a new LocalSession from a registered Registry.
     * <p>
     * The local protocol is also registered on the URL package search path if
     * it's not already.
     * @param namespace The namespace of a {@link LocalSessionRegistry} instance to
     *                  create the new {@link LocalSession}.
     * @return a new LocalSession.
     * @see Handler#registerHandlerIfRequired()
     * @throws SessionLimitReachedException if the number of active sessions
     * are exceeded for the {@link LocalSessionRegistry} implementation.
     * @throws IllegalArgumentException if there is no registry identified by the
     * given namespace, if the given namespace is null, or if the {@link LocalSessionRegistry}
     * implementation fails to provide a <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>
     * compliant base URL for the new session (Such as the hostname being too long
     * due to an excessive combined namespace and session ID length).
     */
    public static LocalSession newSession(String namespace) {
        Handler.registerHandlerIfRequired();
        if (namespace == null) {
            throw new IllegalArgumentException("namespace is null");
        }
        LocalSessionRegistry registry = REGISTRY_MAP.computeIfAbsent(namespace, k -> {
            if (DEFAULT_NAMESPACE.equals(k)) {
                return new DefaultLocalSessionRegistry(DEFAULT_NAMESPACE);
            }
            return null;
        });
        if (registry == null) {
            throw new IllegalArgumentException("Unknown namespace: " + namespace);
        }
        LocalSession ls = registry.newSession();
        try {
            // check the base url is a valid URL
            new URI(ls.getBaseUrl()).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return ls;
    }

    /**
     * Lookup an existing session with the provided local URL hostname.
     * <p>
     * The hostname has two components, the namespace and the sessionId
     * seperated with a period charter. The namespace is assigned when the
     * registry is registered with the {@link LocalProtocol} class, and the
     * sessionId is implementation specific to the registered
     * {@link LocalSessionRegistry}.
     * <p>
     * E.g. {@code local://myregistry.231/path/to/resource.txt}
     *
     * @param urlHost the URL hostname that uniquely identifies an existing
     * LocalSession instance.
     * @return a LocalSession identified by the urlHost, or null of no such
     * session exists.
     */
    protected static LocalSession getSession(String urlHost) {
        String namespace = namespaceOrNull(urlHost);
        if (namespace == null) {
            return null;
        }
        LocalSessionRegistry registry  = REGISTRY_MAP.get(namespace);
        if (registry == null) {
            return null;
        }
        return registry.getSession(urlHost);
    }

    protected static LocalSessionRegistry getRegistry(String namespace) {
        return REGISTRY_MAP.get(namespace);
    }

    protected static String namespaceOrNull(String hostname) {
        if (hostname == null) {
            return null;
        }
        int dotIdx = hostname.indexOf('.');
        if (dotIdx < 0) {
            // no dot found, we won't be able to identify a registry without it.
            return null;
        }
        return hostname.substring(0, dotIdx);
    }
}
