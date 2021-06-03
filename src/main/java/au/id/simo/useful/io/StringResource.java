package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * A Resource implementation that holds a String and provides
 * {@link InputStream}s that reads the String contents.
 */
public class StringResource implements Resource {

    private final String str;
    private final Charset charset;

    /**
     * By default, the UTF-8 Charset is used to convert the provided String into
     * bytes.
     *
     * @param str The source of data for this Resource.
     */
    public StringResource(String str) {
        this.str = str;
        this.charset = Charset.forName("UTF-8");
    }

    /**
     *
     * @param str The source of data for this Resource.
     * @param charset used when converting the provided String into bytes.
     */
    public StringResource(String str, Charset charset) {
        this.str = str;
        this.charset = charset;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream(str.getBytes(charset));
    }

    @Override
    public byte[] getBytes() throws IOException {
        return str.getBytes(charset);
    }

    @Override
    public Reader getReader() throws IOException {
        return new StringReader(str);
    }

    @Override
    public Reader getReader(Charset charset) throws IOException {
        return new StringReader(str);
    }

    @Override
    public String getString() throws IOException {
        return str;
    }

    @Override
    public String getString(Charset charset) throws IOException {
        return str;
    }

    @Override
    public long copyTo(OutputStream out) throws IOException {
        byte[] bytes = str.getBytes(charset);
        out.write(bytes);
        return bytes.length;
    }
}
