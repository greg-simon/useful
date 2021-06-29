package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used for repeatedly reading data from the class path.
 */
public class ClasspathResource implements Resource {

    private final String resourcePath;
    private final ClassLoader classloader;

    /**
     * Looks for the resource using the system class loader.
     * 
     * @param resourcePath the resource to read
     */
    public ClasspathResource(String resourcePath) {
        this.resourcePath = resourcePath;
        this.classloader = ClassLoader.getSystemClassLoader();
    }
    
    public ClasspathResource(ClassLoader classloader, String resourcePath) {
        this.classloader = classloader;
        this.resourcePath = resourcePath;
    }

    @Override
    public InputStream inputStream() throws IOException {
        InputStream in = classloader.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException(
                    "Resource not found on Classpath: "
                    + resourcePath);
        }
        return in;
    }
}
