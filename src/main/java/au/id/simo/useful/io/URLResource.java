package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * A Resource implementation that uses a provided URL as a data source.
 * <p>
 * Following the {@link Resource} principle of no errors until calling
 * {@link #inputStream()}, when constructed with a String url spec it is not
 * validated in any way until used. Repeated calls to {@link #inputStream()}
 * however will reuse the same {@link URL} instance created in the first call.
 */
public class URLResource implements Resource {

    private final String urlString;
    private URL url;

    public URLResource(String url) {
        this.urlString = url;
    }

    public URLResource(URL url) {
        this.urlString = url.toExternalForm();
        this.url = url;
    }

    @Override
    public InputStream inputStream() throws IOException {
        if (url == null) {
            url = URI.create(urlString).toURL();
        }
        return url.openStream();
    }
}
