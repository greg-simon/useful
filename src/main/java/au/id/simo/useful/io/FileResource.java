package au.id.simo.useful.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Allows repeated reads of a file.
 */
public class FileResource implements Resource {

    private final File file;

    public FileResource(File file) {
        this.file = file;
    }

    /**
     * Paths will be normalized to follow relative references such as
     * {@code ../}
     *
     * @param path the first or only part pf the path to the file to read.
     * @param more optional subsequent segments of the file path.
     */
    public FileResource(String path, String... more) {
        Path p = Paths.get(path);
        for(String morePath: more) {
            p = p.resolve(morePath);
        }
        this.file = p.normalize().toFile();
    }

    public FileResource(Path path) {
        this.file = path.toFile();
    }
    
    protected File getFile() {
        return file;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new FileInputStream(file);
    }
}
