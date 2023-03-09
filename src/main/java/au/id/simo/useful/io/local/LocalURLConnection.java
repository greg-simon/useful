package au.id.simo.useful.io.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * URLConnection implementation for the {@code local://} protocol.
 * <p>
 * Underlying InputStreams are sourced from the {@link LocalProtocol} via the
 * referenced {@link LocalSession}.
 */
public class LocalURLConnection extends URLConnection {

    /**
     * Constructor.
     *
     * @param url the URL used to make the connection
     * @see URLConnection#URLConnection(java.net.URL)
     */
    public LocalURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() {
        connected = true;
    }
    
    public boolean isConnected() {
        return connected;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            this.connect();
        }
        // sessionId is the host part of the url:
        // local://sessionid/path
        String sessionIdStr = url.getHost();
        Integer sessionId = LocalProtocol.parseIntOrNull(sessionIdStr);
        LocalSession session = LocalProtocol.getSession(sessionId);
        if (session == null) {
            throw new IOException(String.format(
                    "Unknown local session (Session may have been closed): %s"
                    ,sessionIdStr
            ));
        }
        return session.getInputStream(url.getPath());
    }
}
