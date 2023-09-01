package au.id.simo.useful.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of URLSession that uses the file system for the backing
 * store.
 * <p>
 * It's also useful as a tool in managing temporary files as they are all
 * deleted on closing the session.
 * <p>
 * Any registered Resource, Generator or File will be written to a new file
 * within the session's base directory during registration.
 * <p>
 * Only files created from registration methods are deleted on session close,
 * along with any created directories if they are empty. Any files or
 * directories already existing or created outside of registration methods will
 * remain after closing this session.
 */
public class FileSession implements URLSession {

    private final Path baseDirPath;
    private final Resources resources;
    private final List<Path> createdFileList;
    private final List<Path> createdDirList;
    private final Latch latch;

    /**
     * Creates a new FileSession backed by an auto generated temporary directory
     * starting with 'FS_'.
     * <p>
     * On Linux this is normally {@code /tmp/FS_...}
     *
     * @throws IOException if any errors occur in creating the temp directory.
     */
    public FileSession() throws IOException {
        Path basePath = FileUtil.createTempDirectory("FS_");
        this.baseDirPath = basePath;
        resources = new FileResources(baseDirPath.toFile());
        createdFileList = new ArrayList<>();
        createdDirList = new ArrayList<>();
        latch = new Latch(SESSION_CLOSE_MSG);

        createdDirList.add(basePath);
    }

    /**
     * Creates a new FileSession, backed by the provided directory.
     * <p>
     * No checks on the existence, validity or otherwise of the provided
     * directory is performed. Any errors creating files within it will occur
     * when registering resources.
     *
     * @param baseDir the directory to store all registered resources in. It is
     * expected this exists and is a directory.
     */
    public FileSession(File baseDir) {
        this.baseDirPath = baseDir.toPath();
        resources = new FileResources(baseDir);
        createdFileList = new ArrayList<>();
        createdDirList = new ArrayList<>();
        latch = new Latch(SESSION_CLOSE_MSG);
    }

    /**
     *
     * @return a string URL representation of the directory backing this
     * session.
     * @see File#toURI()
     * @see java.net.URI#toString()
     */
    @Override
    public String getBaseUrl() {
        return baseDirPath.toUri().toString();
    }

    /**
     * @return The base directory registered files will be written to.
     */
    public File getBaseDir() {
        return baseDirPath.toFile();
    }

    /**
     * Provides a hypothetical URL to a resource if one was registered on the
     * provided path.
     * <p>
     * No check is made as to the existence of a registered resource on the
     * provided path.
     *
     * @param path the path to append to the base URL of this session.
     * @return a URL of the {@code file://} protocol that would point to a file
     * if a resource is registered on the path provided.
     */
    @Override
    public String getUrl(String path) {
        return baseDirPath.toUri().resolve(path).toString();
    }

    /**
     * Creates a File from the provided URL path.
     * <p>
     * No checks are made as to the existence of the returned File.
     * <p>
     * All files created here will be deleted on session close.
     *
     * @param path the path to create a new File for. If the first character is
     * '/' then it is removed, as while being a valid start to a relative URL
     * path to register, it translates to the root on the file system which is
     * likely not what the caller intended.
     * @return a File pointing to where the resource on the provided path would
     * be stored.
     * @throws IOException If the directory backing this session is not a
     * directory, or if the path attempts to break out of the backing directory,
     * (i.e "../../../file.txt").
     */
    public File getFile(String path) throws IOException {
        String tempPath = path;
        if (tempPath.startsWith("/")) {
            tempPath = path.substring(1);
        }
        File newFile = FileUtil.newFileInDir(baseDirPath.toFile(), tempPath);
        createdFileList.add(newFile.toPath());
        return newFile;
    }

    /**
     * Creates all the directories to the provided file, if they don't already
     * exist.
     * <p>
     * All directories created here will be deleted on session close, if they
     * are empty at the time. Even if the created directories are outside the
     * base directory.
     * <p>
     * The created directories are not verified to be within the session base
     * directory. It is assumed only files created by getFile() are provided,
     * and they do have their path verified.
     *
     * @param file all missing directories described by the File are created. It
     * is assumed this refers to a file, so only parent directories are created.
     * @throws IOException if there was an error in creating any required
     * directory.
     */
    private void createDirectories(File file) throws IOException {
        if (file.exists()) {
            return;
        }
        Path filePath = file.toPath();
        Path ancestor = filePath.toAbsolutePath().getRoot();
        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            // file must not have a parent, so it must be at the root at the
            // filesystem. So there is no directories to create.
            return;
        }
        for (Path pathSegment : parentPath) {
            ancestor = ancestor.resolve(pathSegment);
            if (Files.exists(ancestor)) {
                continue;
            }
            Files.createDirectory(ancestor);
            createdDirList.add(ancestor);
        }
    }

    @Override
    public String register(String urlPath, Generator product) throws IOException {
        latch.throwIfClosed();
        File outFile = getFile(urlPath);
        createDirectories(outFile);
        FileOutputStream fout = new FileOutputStream(outFile);
        try (OutputStream out = new BufferedOutputStream(fout)) {
            product.writeTo(out);
        }
        return outFile.toURI().toString();
    }

    @Override
    public String register(String urlPath, Resource resource) throws IOException {
        return register(urlPath, resource::copyTo);
    }

    @Override
    public String register(String path, File resource) throws IOException {
        latch.throwIfClosed();
        Path source = resource.toPath();
        File target = getFile(path);
        createDirectories(target);
        Path newFile = Files.copy(
                source,
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );
        return newFile.toUri().toString();
    }

    @Override
    public Resource getResource(String path) throws IOException {
        latch.throwIfClosed();
        return resources.get(path);
    }

    /**
     * Creates a new Set of relative paths for each resource.
     * <p>
     * A relative path is the path of the resource relative to the base
     * directory of this session.
     * <p>
     * For example: A FileSession has a base directory of /tmp/FS_BASE, and a
     * registered resource of path/to/resource.txt.
     * <p>
     * This would result in the resource having a URL of
     * {@code file:///tmp/FS_BASE/path/to/resource.txt }, A path of
     * {@code /tmp/FS_BASE/path/to/resource.txt} and a relative path of
     * {@code path/to/resource.txt}.
     * <p>
     * The created Set will contain the relative path.
     *
     * @return a new Set of relative paths for each resource.
     */
    @Override
    public Set<String> getRegisteredPaths() {
        Set<String> paths = new TreeSet<>();
        // if path separator isn't '/' then swap it, so it is.
        final String pathSeparator = baseDirPath.getFileSystem().getSeparator();
        final boolean switchPathSeparator = !pathSeparator.equals("/");
        createdFileList.forEach(filePath -> {
            Path normPath = baseDirPath.relativize(filePath);
            String normPathStr = normPath.toString();
            if (switchPathSeparator) {
                normPathStr = normPathStr.replace(pathSeparator, "/");
            }
            paths.add(normPathStr);
        });
        return paths;
    }

    @Override
    public boolean isClosed() {
        return latch.isClosed();
    }

    /**
     * Closes this session, deleting only files and directories created by this
     * session.
     * <p>
     * Directories are not deleted unless they are empty.
     *
     * @throws IOException if there is any issues in deleting files or
     * directories.
     */
    @Override
    public void close() throws IOException {
        if (isClosed()) {
            return;
        }
        latch.close();
        for (Path filePath : createdFileList) {
            Files.deleteIfExists(filePath);
        }
        // delete directories in reverse order, to ensure subdirectories are
        // deleted before their parents.
        int lastIndex = createdDirList.size() - 1;
        for (int i = lastIndex; i >= 0; i--) {
            try {
                Path dirToDelete = createdDirList.get(i);
                Files.deleteIfExists(dirToDelete);
            } catch (DirectoryNotEmptyException ex) {
                // ignore if the directory is not empty, as it
                // means something added a file to a dir created
                // by this session.
            }
        }
    }
}
