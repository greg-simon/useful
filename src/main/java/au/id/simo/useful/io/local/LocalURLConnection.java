package au.id.simo.useful.io.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * URLConnection implementation for the {@code local://} protocol.
 *
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
    public void connect() throws IOException {
        connected = true;
    }
    
    public boolean isConnected() {
        return connected;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (connected == false) {
            this.connect();
        }
        // sessionId is the host part of the url:
        // local://sessionid/path
        String sessionId = url.getHost();
        LocalSession session = LocalProtocol.getSession(sessionId);
        if (session == null) {
            throw new IOException(String.format(
                    "Unknown local session (Session may have been closed): %s"
                    ,sessionId
            ));
        }
        return session.getInputStream(url.getPath());
    }
}
