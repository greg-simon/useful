package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Resource implementation that holds a String and provides
 * {@link InputStream}s that reads the String contents.
 */
public class StringResource extends Resource {
    private final String str;

    public StringResource(String str) {
        this.str = str;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream(str.getBytes());
    }
}
