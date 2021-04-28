package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides some helpful file related functions.
 */
public class FileUtil {

    /**
     * Utility classes should not be constructed.
     */
    private FileUtil() {
        ; // no op
    }

    /**
     * Ensures created file is within parent directory.
     * <p>
     * Sanitises the path from potential user input such as "../../" etc.
     *
     * @param parentDir Sets the boundary that the path must be within.
     * @param path Potentially user set string which must resolve to be within
     * the parentDir.
     * @param more Optional further path parts to append to the previous
     * argument. Saves the need to use platform specific path separators
     * manually.
     * @return A new File instance representing the provided path.
     * @throws IOException If the provided path resolves to being outside of the
     * provided parent directory. Or if the parent directory does not exists or
     * is not a directory.
     */
    public static File newFileInDir(File parentDir, String path,
            String... more) throws IOException {
        Path parentDirPath = parentDir.toPath().toAbsolutePath();
        if (!Files.exists(parentDirPath)) {
            throw new IOException(
                    String.format(
                        "Parent directory does not exist: %s"
                        ,parentDirPath
                    )
            );
        }
        if (!Files.isDirectory(parentDirPath)) {
            throw new IOException(
                    String.format(
                        "Parent directory is not a directory: %s"
                        ,parentDirPath
                    )
            );
        }
        Path newFilePath = Paths.get(path, more);
        Path resolvedPath = parentDirPath.resolve(newFilePath);
        Path normalizedPath = resolvedPath.normalize();
        if (!normalizedPath.startsWith(parentDirPath)) {
            throw new IOException("Illegal path: " + path);
        }
        return normalizedPath.toFile();
    }

    /**
     * Creates a new file representing the current working directory of this
     * process.
     *
     * @return the current working directory.
     */
    public static File currentWorkingDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    /**
     * Creates a new file representing the home directory of the user this
     * process is running as.
     *
     * @return the home directory of the current user.
     */
    public static File userHomeDirectory() {
        return new File(System.getProperty("user.home"));
    }
}
