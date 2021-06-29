package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Resource implementation based on a byte array.
 */
public class ByteArrayResource implements Resource {

    private final byte[] data;

    public ByteArrayResource(byte[] data) {
        this.data = data;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }
}
