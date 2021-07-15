package au.id.simo.useful.io.local;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import au.id.simo.useful.io.ConcurrentGeneratorResource;
import au.id.simo.useful.io.FileResource;
import au.id.simo.useful.io.Generator;
import au.id.simo.useful.io.Resource;
import au.id.simo.useful.io.URLSession;
import au.id.simo.useful.io.CloseStatus;

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

    private final Integer id;
    private final Map<String, Resource> resourceMap;
    /**
     * Tracks session close status.
     */
    private final CloseStatus closeStatus;

    /**
     * Only the LocalProtocol class should create LocalSession instances.
     *
     * @param id The assigned sessionId of this session. This is required to
     * build any full URL of this session.
     */
    protected LocalSession(Integer id) {
        this.id = id;
        resourceMap = new HashMap<>();
        closeStatus = new CloseStatus(SESSION_CLOSE_MSG);
    }

    /**
     * Gets the session id of this instance.
     *
     * @return this instances session id.
     */
    public Integer getId() {
        return id;
    }

    @Override
    public String getBaseUrl() {
        return urlFromPath("");
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
        return String.format("local://%s/%s", id, normalisedPath);
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
        closeStatus.throwIfClosed();
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
        closeStatus.throwIfClosed();
        return resourceMap.get(normalisePath(path));
    }

    @Override
    public Set<String> getRegisteredPaths() {
        return new TreeSet<>(resourceMap.keySet());
    }

    @Override
    public boolean isClosed() {
        return closeStatus.isClosed();
    }

    @Override
    public void close() throws IOException {
        closeStatus.close();
        LocalProtocol.unregisterSession(this);
    }

    /**
     * Ensure paths don't start with a '/'.
     *
     * @param path the path to check for the correct leading character.
     * @return a path that will never start with '/'.
     */
    private static String normalisePath(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
