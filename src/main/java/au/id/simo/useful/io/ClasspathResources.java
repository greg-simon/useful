package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A Resources implementation that will obtain resources from the class path.
 */
public class ClasspathResources extends Resources {

    private final ClassLoader classLoader;

    /**
     * Constructs a Resources instance that uses the system ClassLoader to look
     * up resources.
     * <p>
     * No pattern is used to resolve resource names.
     */
    public ClasspathResources() {
        this(null, null);
    }

    /**
     * Constructs a Resources instance that uses the system ClassLoader to look
     * up resources.
     *
     * @param pattern The {@code Formatter} pattern to use in resolving the
     * resource name in the method
     * {@link Resources#get(java.lang.Object, java.lang.Object...)}.
     */
    public ClasspathResources(String pattern) {
        this(null, pattern);
    }

    /**
     * Construct a Resources instance that loads resources from the class path,
     * using the provided ClassLoader.
     *
     * @param classLoader Can be null. Used to lookup resources. If null, the
     * system ClassLoader is used.
     * @param pattern The {@code Formatter} pattern to use in resolving the
     * resource name in the method
     * {@link Resources#get(java.lang.Object, java.lang.Object...)}.
     */
    public ClasspathResources(ClassLoader classLoader, String pattern) {
        super(pattern);
        if (classLoader == null) {
            this.classLoader = ClassLoader.getSystemClassLoader();
        } else {
            this.classLoader = classLoader;
        }
    }

    @Override
    protected InputStream createStream(String name) throws IOException {
        InputStream in = classLoader.getResourceAsStream(name);
        if (in == null) {
            throw new IOException("Resource not found on Classpath: " + name);
        }
        return in;
    }

    @Override
    protected boolean exists(String resolvedResourceName) {
        URL resURL = classLoader.getResource(resolvedResourceName);
        return resURL != null;
    }
}
