package au.id.simo.useful.io.local;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Registration point for the custom {@code local://} protocol.
 * <p>
 * To register a custom protocol that {@link URL} can understand, A class named
 * {@code Handler} that extends {@link URLStreamHandler} must exist in a package
 * where the last part of the package name is the protocol label. In this case,
 * the last part of the package name must be 'local'.
 * <p>
 * This package name (not including the last part matching the protocol name)
 * must then be registered on the system property
 * {@code "java.protocol.handler.pkgs"}, so the URL class knows where to look
 * for protocol handlers.
 * <p>
 * If there are multiple packages to search, they are delimited by the '|'
 * character.
 * <p>
 * The search of packages for a protocol handler is performed in the order they
 * are listed. The first matching protocol handler found is used.
 */
public class Handler extends URLStreamHandler {

    private static final String URL_HANDLER_PACKAGE = "au.id.simo.useful.io";

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new LocalURLConnection(u);
    }

    /**
     * Registers the Local URL Handler with the {@code URL} package search path.
     * This method must be call prior to creating a URL using the
     * {@code local://} protocol.
     * <p>
     * This is safe to call multiple times.
     */
    public static void registerHandlerIfRequired() {
        String existingSearchPath = System.getProperty("java.protocol.handler.pkgs", "");
        if (existingSearchPath.contains(URL_HANDLER_PACKAGE)) {
            return;
        }
        if (existingSearchPath.isEmpty()) {
            System.setProperty("java.protocol.handler.pkgs", URL_HANDLER_PACKAGE);
        } else {
            // prepend local package on the handler search path
            System.setProperty(
                    "java.protocol.handler.pkgs",
                    String.join("", URL_HANDLER_PACKAGE, "|", existingSearchPath)
            );
        }
    }
}
