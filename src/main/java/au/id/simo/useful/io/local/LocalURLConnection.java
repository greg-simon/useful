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
        // The hostname is the namespace and the session id seperated with a period character.
        // local://namespace.sessionId/path
        String hostname = url.getHost();
        LocalSession session = LocalProtocol.getSession(hostname);
        if (session == null) {
            throw new IOException(String.join(" ",
                    "Unknown local session (Session may have been closed):"
                    ,hostname
            ));
        }
        return session.getInputStream(url.getPath());
    }
}
