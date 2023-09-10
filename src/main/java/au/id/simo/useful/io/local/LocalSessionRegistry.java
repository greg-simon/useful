package au.id.simo.useful.io.local;

public interface LocalSessionRegistry {
    String getNamespace();
    LocalSession newSession();
    LocalSession getSession(String urlHost);
    default String getSessionIdOrNull(String hostname) {
        if (hostname == null) {
            return null;
        }
        int dotIdx = hostname.indexOf('.');
        if (dotIdx < 0) {
            // no dot found, we won't be able to identify a sessionId without it.
            return null;
        }
        return hostname.substring(dotIdx + 1);
    }
    void unregisterSession(LocalSession session);
    int closeAllSessions();
    int capacity();
    int size();
}
