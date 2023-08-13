package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * A Resources implementation that will obtain resources from the file system.
 */
public class FileResources extends Resources {

    private final File parentDir;

    /**
     * Constructs a Resources instance that looks up resources from the current
     * working directory.
     * <p>
     * No naming pattern is used.
     */
    public FileResources() {
        this(FileUtil.currentWorkingDirectory(), null);
    }

    /**
     * Constructs a Resources instance that looks up resources from the current
     * working directory.
     *
     * @param namePattern The {@code Formatter} pattern to use in resolving the
     * resource name in the method
     * {@link Resources#get(java.lang.Object, java.lang.Object...)}.
     */
    public FileResources(String namePattern) {
        this(FileUtil.currentWorkingDirectory(), namePattern);
    }

    /**
     * Constructs a Resources instance that looks up resources from the provided
     * directory.
     * <p>
     * No naming pattern is used.
     *
     * @param parentDir the directory to look up files to load resources from.
     * It is assumed this is a directory as no checks for existence, validity or
     * availability are made until a Resource created access attempted.
     */
    public FileResources(File parentDir) {
        this(parentDir, null);
    }

    /**
     * Constructs a Resources instance that looks up resources from the provided
     * directory.
     *
     * @param parentDir the directory to look up files to load resources from.
     * It is assumed this is a directory as no checks for existence, validity or
     * availability are made until a Resource created access attempted.
     * @param pattern The {@code Formatter} pattern to use in resolving the
     * resource name in the method
     * {@link Resources#get(java.lang.Object, java.lang.Object...)}.
     */
    public FileResources(File parentDir, String pattern) {
        super(pattern);
        this.parentDir = parentDir;
    }

    @Override
    protected InputStream createStream(String resolvedResourceName) throws IOException {
        File file = FileUtil.newFileInDir(parentDir, resolvedResourceName);
        return Files.newInputStream(file.toPath());
    }

    @Override
    protected boolean exists(String resolvedResourceName) {
        try {
            File file = FileUtil.newFileInDir(parentDir, resolvedResourceName);
            return file.exists();
        } catch (IOException ex) {
            return false;
        }
    }
}
