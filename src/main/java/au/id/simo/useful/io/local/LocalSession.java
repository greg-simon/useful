package au.id.simo.useful.io.local;

import au.id.simo.useful.io.ConcurrentGeneratorResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import au.id.simo.useful.io.FileResource;
import au.id.simo.useful.io.Generator;
import au.id.simo.useful.io.Resource;
import au.id.simo.useful.io.URLSession;

/**
 * An implementation of the URLSession that uses the local protocol
 * ({@code local://}) to provide URL access to registered resources.
 * <p>
 * This class should not be instantiated directly, but obtained from the
 * {@link LocalProtocol} class.
 *
 * @see LocalProtocol#newSession()
 */
public class LocalSession implements URLSession {

    private final String id;
    private final Map<String, Resource> resourceMap;
    private boolean closed;

    /**
     * Only the LocalProtocol class should create LocalSession instances.
     *
     * @param id The assigned sessionId of this session. This is required to
     * build any full URL of this session.
     */
    protected LocalSession(String id) {
        this.id = id;
        resourceMap = new HashMap<>();
        closed = false;
    }

    /**
     * Gets the session id of this instance.
     *
     * @return this instances session id.
     */
    public String getId() {
        return id;
    }

    @Override
    public String getBaseUrl() {
        return urlFromPath("/");
    }

    @Override
    public String getUrl(String path) {
        return urlFromPath(normalisePath(path));
    }

    /**
     * Creates a full URL from the provided path.
     * <p>
     * There is no guarantee that a resource is registered for this path.
     *
     * @param normalisedPath a path that will start with the '/' character.
     * @return a full URL to a resource.
     */
    private String urlFromPath(String normalisedPath) {
        return String.format("local://%s%s", id, normalisedPath);
    }

    /**
     * Registered Generators are wrapped in a
     * {@link ConcurrentGeneratorResource} for generation when accessed.
     *
     * @param urlPath the relative path to register this Generator.
     * @param generator the Generator that provides data when requested via a
     * URL.
     * @return A URL that points to the registered Generator.
     * @throws java.io.IOException if there is an issue in reading data from the
     * Generator.
     * @see ConcurrentGeneratorResource
     */
    @Override
    public String register(String urlPath, Generator generator) throws IOException {
        return register(urlPath, new ConcurrentGeneratorResource(generator));
    }

    @Override
    public String register(String urlPath, Resource resource) throws IOException {
        if (closed) {
            throw new IOException("Session is closed");
        }
        String normalisedPath = normalisePath(urlPath);
        resourceMap.put(normalisedPath, resource);
        return urlFromPath(normalisedPath);
    }

    @Override
    public String register(String path, File file) throws IOException {
        return register(path, new FileResource(file));
    }

    @Override
    public Resource getResource(String path) throws IOException {
        if (closed) {
            throw new IOException("Session is closed");
        }
        return resourceMap.get(normalisePath(path));
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        Resource ressource = getResource(path);
        if (ressource == null) {
            throw new IOException("No resource registered on path: " + path);
        }
        return ressource.inputStream();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * A close method for use by the Local Registry that doesn't throw an
     * Exception just for setting a flag.
     */
    protected void closeLocalSession() {
        closed = true;
    }

    @Override
    public void close() throws IOException {
        LocalProtocol.closeSession(this);
    }

    /**
     * Ensure all paths start with a '/'.
     *
     * @param path the path to check for the correct leading character.
     * @return a path that will always start with '/'.
     */
    private static String normalisePath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }
}
