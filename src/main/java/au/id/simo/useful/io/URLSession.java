package au.id.simo.useful.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Provides a way to temporarily arrange resources for the purpose of rendering
 * a web page using URLs.
 * <p>
 * Usage Example:
 * <pre>
 * try (URLSession session = ...)
 * {
 *     session.register("index.html", new File("my-app.html"));
 *     session.register("scripts/app.js", new File("app.js"));
 *     session.register("images/logo.png", new File("bom_logo.png"));
 *     session.register("style.css", new File("app-style.css"));
 *
 *     String indexUrl = session.getUrl("index.html");
 *     URL url = new URL(indexUrl);
 *
 *     // use renderer here.
 *     HtmlToPDF.generate(url, new File("output.pdf"));
 * } // session close
 * </pre>
 * <p>
 * If a resource is registered on a path that already has a registered resource,
 * then the existing resource is silently replaced with the new one.
 * <p>
 * Any resources are to be cleaned up when the session is closed. Any attempt to
 * obtain registered resources after the session is closed, should result in an
 * IOException.
 */
public interface URLSession extends Closeable {

    static final String SESSION_CLOSE_MSG = "Session is closed";

    /**
     * Obtains a string representation of base URL of this session.
     * <p>
     * The full URL spec provided is implementation specific, but will be usable
     * with {@link java.net.URL}.
     *
     * @return the URL that registered resources paths are relative to.
     */
    String getBaseUrl();

    /**
     * Obtains a full String URL of the provided path.
     * <p>
     * Example:
     * <pre><code>
     * String fullUrlStr = session.getUrl("index.html");
     * URL url = new URL(fullUrlStr);
     * </code></pre>
     *
     * The full URL spec provided is implementation specific, but will be usable
     * with {@link java.net.URL}.
     *
     * @param path a relative path to a resource.
     * @return A string URL suitable for use with {@link java.net.URL} to point
     * to the specified relative path.
     */
    String getUrl(String path);

    /**
     * Registers a Generator on the provided path.
     *
     * @param path the path, relative to this sessions base URL, to register
     * this product on.
     * @param product The Generator that will provide the data when the URL is
     * requested.
     * @return The full URL spec to the registered product, as if
     * {@code getUrl(path)} was called.
     * @throws IOException if there is an error in registering the provided
     * product.
     * @see URLSession#getUrl(java.lang.String)
     */
    String register(String path, Generator product) throws IOException;

    /**
     * Registers a Resource on the provided path.
     *
     * @param path the path, relative to this sessions base URL, to register
     * this product on.
     * @param resource The Resource that will provide the data when the URL is
     * requested.
     * @return The full URL spec to the registered resource, as if
     * {@code getUrl(path)} was called.
     * @throws IOException if there is an error in registering the provided
     * resource.
     * @see URLSession#getUrl(java.lang.String)
     */
    String register(String path, Resource resource) throws IOException;

    /**
     * Registers a Resource on the provided path.
     * <p>
     * It is implementation specific if the provided file is checked or accessed
     * within this method.
     *
     * @param path the path, relative to this sessions base URL, to register
     * this File on.
     * @param resource The File that will provide the data when the URL is
     * requested.
     * @return The full URL spec to the registered file, as if
     * {@code getUrl(path)} was called.
     * @throws IOException if there is an error in registering the provided
     * file.
     * @see URLSession#getUrl(java.lang.String)
     */
    String register(String path, File resource) throws IOException;

    /**
     * Obtains a resource registered to the provided path.
     *
     * @param path the path the requested resource was registered to, relative
     * to the session base URL.
     * @return The resource registered to the provided path. Or null of no
     * resource was found with the path.
     * @throws IOException If there was an error in obtaining the registered
     * resource, or if this session has been closed.
     */
    Resource getResource(String path) throws IOException;

    /**
     * Obtains an InputStream to read data from the resource registered to the
     * provided path.
     *
     * @param path the path the requested resource was registered to, relative
     * to the session base URL.
     * @return An InputStream to read data from the resource registered to the
     * provided path. If no resource is found, then null is returned.
     * @throws IOException If a resource is not found on the provided path, if
     * there was an error in reading the resource, or of this session has been
     * closed.
     */
    default InputStream getInputStream(String path) throws IOException {
        if (isClosed()) {
            throw new IOException(SESSION_CLOSE_MSG);
        }
        Resource resource = getResource(path);
        if (resource == null) {
            return null;
        }
        return resource.inputStream();
    }

    /**
     * Obtain a set of relative paths that have been registered with this
     * URLSession.
     * <p>
     * It allows for iterating over all resources registered along with the
     * {@link #getResource(java.lang.String)} or
     * {@link #getInputStream(java.lang.String)} methods.      <code>
     * for (String path: urlSession.getgetRegisteredPaths()) {
     *    Resource res = urlSession.getResource(path);
     *    ...
     * }
     * </code>
     *
     * Paths returned are relative to the base url of the session, so will not
     * contain a leading '/' character.
     *
     * @return a set of paths that have been registered with this URLSession.
     * Each path may have been normalised and may not be the exact String used
     * during registration. If no paths have been registered, then an empty Set
     * will be returned.
     */
    Set<String> getRegisteredPaths();

    /**
     * Obtains the closed status of this session.
     *
     * @return false if the session is still available for use, and true if the
     * session has been closed.
     */
    boolean isClosed();

    /**
     * Functional method to allow an action to be taken for each registered
     * Resource.
     *
     * @param consumer
     * @throws IOException
     */
    default void forEachResource(ResourceCollector consumer) throws IOException {
        for (String path : this.getRegisteredPaths()) {
            Resource res = this.getResource(path);
            consumer.add(path, res);
        }
    }

    @FunctionalInterface
    interface ResourceCollector {

        void add(String relativePath, Resource resource) throws IOException;
    }
}
