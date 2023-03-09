package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides some helpful file related functions.
 */
public class FileUtil {

    /**
     * Utility classes should not be constructed.
     */
    private FileUtil() {
        // no op
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
     * @throws IOException If the provided path resolves to being outside the
     * provided parent directory. Or if the parent directory does not exist or
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
     * Creates a temporary directory and sets permissions so only the current
     * user has access.
     * <p>
     * Only works for POSIX and ACL (Access Control List) file systems.
     * <p>
     * The new temp directory is created within the directory provided by
     * {@code System.getProperty("java.io.tmpdir")}
     *
     * @param prefix The first part of the name of the new directory.
     * @return The newly created directory.
     * @throws java.io.IOException if the directory cannot be created, or the
     * permissions cannot be set.
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir"));
        return createTempDirectory(tmpPath, prefix);
    }
    
    /**
     * Creates a temporary directory and sets permissions so only the current
     * user has access.
     * <p>
     * Only works for POSIX and ACL (Access Control List) file systems.
     * 
     * @param tmpPath The path to the directory in which to create a new
     * directory.
     * @param prefix The first part of the name of the new directory.
     * @return The newly created directory.
     * @throws java.io.IOException if the directory cannot be created, or the
     * permissions cannot be set.
     */
    public static Path createTempDirectory(Path tmpPath, String prefix) throws IOException {
        FileStore fs = Files.getFileStore(tmpPath);
        if (fs.supportsFileAttributeView(PosixFileAttributeView.class)) {
            return createTempDirectoryPosix(tmpPath, prefix);
        }
        if (fs.supportsFileAttributeView(AclFileAttributeView.class)) {
            return createTempDirectoryAcl(tmpPath, prefix);
        }
        throw new IOException("Unsupported file system: Does not support POSIX"
                + " or Access Control List (ACL) file permissions");
    }
    
    protected static Path createTempDirectoryPosix(Path tmpPath, String prefix) throws IOException {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        FileAttribute<Set<PosixFilePermission>> fileAttr = PosixFilePermissions.asFileAttribute(permissions); 
        return Files.createTempDirectory(tmpPath, prefix, fileAttr);
    }
    
    protected static Path createTempDirectoryAcl(Path tmpPath, String prefix) throws IOException {
        FileSystem fileSystem = tmpPath.getFileSystem();
        // will fail on posix file systems
        UserPrincipalLookupService userLookup =  fileSystem.getUserPrincipalLookupService();
        // obtain user principle
        UserPrincipal userPrincipal = userLookup.lookupPrincipalByName(System.getProperty("user.name"));
        //Give all permissions to user, and no one else.
        List<AclEntry> entryList = Collections.singletonList(
            AclEntry.newBuilder()
                .setPrincipal(userPrincipal)
                .setPermissions(AclEntryPermission.values())
                .setFlags(
                    AclEntryFlag.DIRECTORY_INHERIT,
                    AclEntryFlag.FILE_INHERIT
                ).setType(AclEntryType.ALLOW)
                .build()
        );
        FileAttribute<List<AclEntry>> tempDirAttr =  new FileAttribute<List<AclEntry>>() {
            @Override
            public String name() {
                return "acl:acl";
            }

            @Override
            public List<AclEntry> value() {
                return entryList;
            }
        };
        return Files.createTempDirectory(tmpPath, prefix, tempDirAttr);
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
