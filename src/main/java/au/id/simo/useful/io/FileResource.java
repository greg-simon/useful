package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Allows repeated reads of a file.
 */
public class FileResource implements Resource {

    private final Path path;

    public FileResource(File file) {
        this.path = file.toPath();
    }

    /**
     * Paths will be normalized to follow relative references such as
     * {@code ../}
     *
     * @param path the first or only part of the path to the file to read.
     * @param more optional subsequent segments of the file path.
     */
    public FileResource(String path, String... more) {
        Path p = Paths.get(path);
        for(String morePath: more) {
            p = p.resolve(morePath);
        }
        this.path = p.normalize();
    }

    public FileResource(Path path) {
        this.path = path;
    }

    protected Path getPath() {
        return path;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return Files.newInputStream(path);
    }
}
