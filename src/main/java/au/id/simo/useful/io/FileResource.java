package au.id.simo.useful.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class FileResource extends Resource {

    private final File file;

    public FileResource(File file) {
        this.file = file;
    }

    public FileResource(String path, String... more) {
        this.file = Paths.get(path, more).toFile();
    }

    public FileResource(Path path) {
        this.file = path.toFile();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new FileInputStream(file);
    }
}
