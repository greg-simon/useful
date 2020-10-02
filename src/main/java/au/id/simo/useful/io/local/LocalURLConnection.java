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
public class LocalURLConnection extends URLConnection
{
	/**
	 * Constructor.
	 * 
	 * @param url the URL used to make the connection
	 * @see URLConnection#URLConnection(java.net.URL)
	 */
	public LocalURLConnection(URL url)
	{
		super(url);
	}

	@Override
	public void connect() throws IOException
	{
		super.connected = true;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		// sessionId is the host part of the url:
		// local://sessionid/path
		String sessionId = url.getHost();
		LocalSession session = LocalProtocol.getSession(sessionId);
		if (session == null) {
			throw new IOException("Unknown local session: " +
				sessionId);
		}
		return session.getInputStream(url.getPath());
	}
}
