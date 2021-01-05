package au.id.simo.useful.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of URLSession that uses the file system for the backing
 * store.
 * <p>
 * Any registered Resource, Generator or File will be written to a new file
 * within the base directory apon registration.
 * <p>
 * Only files created during registration are deleted on session close, as are
 * any created directories if they are empty.
 */
public class FileSession implements URLSession {

    private static final String SESSION_CLOSE_MSG = "Session is closed";

    private final File baseDir;
    private final Resources resources;
    private final List<Path> createdFileList;
    private final List<Path> createdDirList;
    private volatile boolean closed;

    /**
     * Creates a new FileSession backed by an auto generated temporary directory
     * starting with 'FS_'.
     * <p>
     * On Linux this is normally {@code /tmp/FS_...}
     *
     * @throws IOException if any errors occur in creating the temp directory.
     */
    public FileSession() throws IOException {
        Path basePath = Files.createTempDirectory("FS_");
        this.baseDir = basePath.toFile();
        resources = new FileResources(baseDir);
        createdFileList = new ArrayList<>();
        createdDirList = new ArrayList<>();
        closed = false;
        
        createdDirList.add(basePath);
    }

    /**
     * Creates a new FileSession, backed by the provided directory.
     * <p>
     * No checks on the existence, validity or otherwise of the provided
     * directory is performed. Any errors creating files within it will occur
     * when registering resources.
     *
     * @param baseDir the directory to store all registered resources in.
     */
    public FileSession(File baseDir) {
        this.baseDir = baseDir;
        resources = new FileResources(baseDir);
        createdFileList = new ArrayList<>();
        createdDirList = new ArrayList<>();
        closed = false;
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
        return baseDir.toURI().toString();
    }
    
    /**
     * @return The base directory registered files will written to.
     */
    public File getBaseDir() {
        return baseDir;
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
        return baseDir.toURI().resolve(path).toString();
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
        File newFile = FileUtil.newFileInDir(baseDir, tempPath);
        createdFileList.add(newFile.toPath());
        return newFile;
    }

    /**
     * Creates all the directories to the provided file, if they don't already
     * exists.
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
        Path ancestor = file.toPath().toAbsolutePath().getRoot();
        for (Path pathSegment : file.toPath().getParent()) {
            ancestor = ancestor.resolve(pathSegment);
            if (Files.exists(ancestor)) {
                continue;
            }
            Files.createDirectory(ancestor);
            createdDirList.add(ancestor);
        }
    }

    @Override
    public String register(String urlPath, Generator product)
            throws IOException {
        if (isClosed()) {
            throw new IOException(SESSION_CLOSE_MSG);
        }
        File outFile = getFile(urlPath);
        createDirectories(outFile);
        try (OutputStream out = new BufferedOutputStream(
                new FileOutputStream(outFile))) {
            product.writeTo(out);
        }
        return outFile.toURI().toString();
    }

    @Override
    public String register(String urlPath, Resource resource)
            throws IOException {
        return register(urlPath, (OutputStream out) -> {
            resource.copyTo(out);
        });
    }

    @Override
    public String register(String path, File resource) throws IOException {
        if (isClosed()) {
            throw new IllegalStateException(SESSION_CLOSE_MSG);
        }
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
        if (isClosed()) {
            throw new IOException(SESSION_CLOSE_MSG);
        }
        return resources.get(path);
    }

    @Override
    public InputStream getInputStream(String path)
            throws IOException {
        Resource prod = getResource(path);
        return prod.inputStream();
    }

    @Override
    public boolean isClosed() {
        return closed;
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
        closed = true;
        for (Path filePath : createdFileList) {
            Files.deleteIfExists(filePath);
        }
        // delete directories in reverse order, to ensure subdirs are
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
                ;
            }
        }
    }
}
