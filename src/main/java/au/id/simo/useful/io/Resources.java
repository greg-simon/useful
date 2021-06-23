package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to help load {@link Resource}s using a naming pattern.
 * <p>
 * It uses the {@link java.util.Formatter} template system to resolve resource
 * names. Typically '%s' in the pattern provided to the constructor, and it is
 * formatted with resource name arguments in the
 * {@link #get(java.lang.Object, java.lang.Object...)} method.
 * <p>
 * Template pattern example:
 * <pre>
 * Resources resources = new FileResources("%s.txt");
 * Resource readme = resources.get("README"); // README.txt
 * Resource notes = resources.get("notes"); // notes.txt
 * </pre>
 * <p>
 * An equivalent example, without using a template pattern:
 * <pre>
 * Resources resources = new FileResources();
 * Resource readme = resources.get("README.txt");
 * Resource notes = resources.get("notes.txt");
 * </pre>
 * <p>
 * Any byte to String conversion will assume UTF-8.
 * <p>
 * Further Example Usage:<br>
 * To load the contents of 'my-report.template' from the system class path as a
 * String, you could code it like the following:
 * <pre>
 * Resources clResources = new ClasspathResources("%s.template");
 * Resource myReportTemplateRes = clResources.get("my-report");
 * </pre>
 * <p>
 * To load .gif files from a directory (/path/to/images/my-image.gif):
 * <pre>
 * File templateDir = new File("/path/to/images/");
 * Resources imageResources = new FileResources(templateDir, "%s.gif");
 * Resource imageRes =  imageResources.get("my-image");
 * </pre>
 *
 * @see FileResources
 * @see ClasspathResources
 * @see Resource
 */
public abstract class Resources {

    /**
     * Can be null, but if not null then it must contain at least one '%s'.
     */
    private final String resourcePattern;

    /**
     * Constructor.
     * <p>
     * If there is no argument marker '%' present, '%s' will be appended to the
     * provided pattern.
     * <p>
     * Examples with identical behavior:
     * <pre>
     * new Resources("path/to/resources/%s").get("name");
     * new Resources("path/to/resources/").get("name");
     * </pre>
     *
     * @param pattern Can be null. The pattern to use with resource names when
     * loading resources.
     */
    protected Resources(String pattern) {
        String tempPat = pattern;
        if (pattern != null && !pattern.contains("%")) {
            tempPat = pattern + "%s";
        }
        this.resourcePattern = tempPat;
    }

    /**
     * Obtain a resource from the implemented backing store of data.
     * <p>
     * No IO is attempted until methods on the returned Resource are called.
     *
     * @param resourceName At least one object is required to apply to the
     * Formatter pattern to resolve the resource name. If no pattern exists,
     * this argument is returned as a String.
     * @param otherArgs Other optional arguments to apply to the Formatter
     * pattern.
     * @return A Resource that is able to create an InputStream based on the
     * resource name and class scoped pattern. If no resource is found, null is
     * returned.
     * @throws java.util.IllegalFormatException see {@link java.util.Formatter}
     * for further details on when exceptions are thrown.
     */
    public Resource get(Object resourceName, Object... otherArgs) {
        String resovledResourceName = resolveResourceName(resourceName, otherArgs);
        if (!exists(resovledResourceName)) {
            return null;
        }
        return new Resource() {
            @Override
            public InputStream inputStream() throws IOException {
                return createStream(resovledResourceName);
            }
        };
    }

    /**
     * Applies the resource name to the class scoped pattern to obtain the full
     * name of the Resource.
     *
     * @param resourceName At least one Object is required to resolve a resource
     * name.
     * @param otherArgs Additional Objects to apply to the Formatter pattern to
     * resolve the resource name.
     * @return the full name of the resource.
     * @throws java.util.IllegalFormatException see {@link java.util.Formatter}
     * for further details on when exceptions are thrown.
     */
    protected String resolveResourceName(Object resourceName, Object... otherArgs) {
        if (resourcePattern == null) {
            return String.valueOf(resourceName);
        }

        if (otherArgs == null || otherArgs.length == 0) {
            return String.format(resourcePattern, resourceName);
        }
        // create a single array for all naming args for use by String.format().
        Object[] allArgs = new Object[otherArgs.length + 1];
        allArgs[0] = resourceName;
        System.arraycopy(otherArgs, 0, allArgs, 1, otherArgs.length);
        return String.format(resourcePattern, allArgs);
    }

    /**
     * Creates an InputStream of the resource identified by the expanded name
     * provided.
     * <p>
     * Implementations are to never return null, instead an IOException should
     * be thrown if a resource is unable to be provided.
     *
     * @param resolvedResourceName the full resource name that has already been
     * resolved from the Formatter pattern and any {@code get()} arguments.
     * @return a newly created InputStream of the named resource
     * @throws IOException if there is an error in creating the InputStream, or
     * if the named resource could not be found, or is otherwise unavailable.
     */
    protected abstract InputStream createStream(String resolvedResourceName) throws IOException;

    /**
     * Used to flag if the underlying resource identified by the resolved
     * resource name exists.
     *
     * @param resolvedResourceName the full resource name that has already been
     * resolved from the Formatter pattern and any {@code get()} arguments.
     * @return True if the underlying resource identified by the resolved
     * resource name exists. False is returned if no resource was found, or
     * there was an error in querying the underlying implementation.
     */
    protected abstract boolean exists(String resolvedResourceName);
}
