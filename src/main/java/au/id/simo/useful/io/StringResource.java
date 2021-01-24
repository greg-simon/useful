package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

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

    @Override
    public byte[] getBytes() throws IOException {
        return str.getBytes();
    }

    @Override
    public Reader getReader() throws IOException {
        return new StringReader(str);
    }

    @Override
    public String getString() throws IOException {
        return str;
    }

    @Override
    public long copyTo(OutputStream out) throws IOException {
        byte[] bytes = str.getBytes();
        out.write(bytes);
        return bytes.length;
    }
}
