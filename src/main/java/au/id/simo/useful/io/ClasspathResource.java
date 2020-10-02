package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ClasspathResource extends Resource {

    private final String resourcePath;

    public ClasspathResource(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public InputStream inputStream() throws IOException {
        InputStream in = ClassLoader.getSystemResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException(
                    "Resource not found on Classpath: "
                    + resourcePath);
        }
        return in;
    }
}
