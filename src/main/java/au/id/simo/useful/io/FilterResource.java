package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Provides a class to delegate all methods to the provided backing Resource.
 */
public class FilterResource implements Resource {
    protected final Resource resource;

    public FilterResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Reader getReader() throws IOException {
        return resource.getReader();
    }

    @Override
    public Reader getReader(Charset charset) throws IOException {
        return resource.getReader(charset);
    }

    @Override
    public long copyTo(OutputStream out) throws IOException {
        return resource.copyTo(out);
    }

    @Override
    public InputStream inputStream() throws IOException {
        return resource.inputStream();
    }
}
