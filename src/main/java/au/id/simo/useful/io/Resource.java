package au.id.simo.useful.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * An {@link InputStream} factory. Providing unified access to a source of bytes
 * that can be read repeatedly.
 * <p>
 * No attempt is to be made to test the existence or validity of the underlying
 * resource at instance creation time.
 * <p>
 * Any attempt to read a nonexistent resource should result in an IOException,
 * or a subclass, being thrown.
 * <p>
 * All character related methods ({@code getString()}, {@code getReader()})
 * assume UTF-8 as the character encoding, unless another {@link Charset} is
 * provided.
 */
public interface Resource {

    /**
     * This is the default {@link Charset} used for all bytes to chars
     * conversions methods, unless another {@link Charset} is provided as an
     * argument.
     * 
     * @see #getReader()
     * @see #getString()
     */
    public static final Charset DEFAULT_CHARSET_UTF8 = StandardCharsets.UTF_8;

    /**
     * Loads the contents of the resource as a String.
     * <p>
     * It is assumed that UTF-8 is the character encoding.
     *
     * @return the full contents of the resource, as a String. Cannot be null.
     * @throws IOException if there is any errors in finding or reading the
     * resource.
     */
    default String getString() throws IOException {
        return getString(DEFAULT_CHARSET_UTF8);
    }

    /**
     * Loads the contents of the resource as a String.
     *
     * @param charset The character set to use in decoding the characters.
     * @return the full contents of the resource, as a String. Cannot be null.
     * @throws IOException if there is any errors in finding or reading the
     * resource.
     */
    default String getString(Charset charset) throws IOException {
        return new String(getBytes(), charset);
    }

    /**
     * Creates a Reader to read the resources contents.
     * <p>
     * It is assumed that UTF-8 is the character encoding.
     *
     * @return a Reader which will read the contents of the resource
     * @throws IOException if there is any errors in creating a Reader of the
     * resource.
     */
    default Reader getReader() throws IOException {
        return getReader(DEFAULT_CHARSET_UTF8);
    }

    /**
     * Creates a Reader to read the resources contents.
     *
     * @param charset The character set to use in decoding the characters.
     * @return a Reader which will read the contents of the resource
     * @throws IOException if there is any errors in creating a Reader of the
     * resource.
     */
    default Reader getReader(Charset charset) throws IOException {
        return new InputStreamReader(inputStream(), charset);
    }

    /**
     * Loads the contents of the resource as a {@code byte} array.
     *
     * @return the full contents of the resource, as a byte array.
     * @throws IOException if there is any errors in reading the resource.
     */
    default byte[] getBytes() throws IOException {
        try (InputStream in = inputStream()) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(in, bout);
            return bout.toByteArray();
        }
    }

    /**
     * A convenience method that copies bytes from this instance's InputStream
     * to the OutputStream provided.
     * <p>
     * NOTE: From Java 9+ this will be redundant due to
     * {@code InputStream.transferTo(OutputStream)}
     *
     * @param out Underlying bytes this resource refers to is written to this
     * OutputStream
     * @return the number of bytes written to the provided OutputStream
     * @throws IOException if there was an issue in reading from this resource's
     * bytes, or writing to the provided OutputStream
     */
    default long copyTo(OutputStream out) throws IOException {
        try (InputStream in = inputStream()) {
            return IOUtils.copy(in, out);
        }
    }

    /**
     * Creates an InputStream to read the contents of the resource.
     *
     * @return a new InputStream to read the underlying source of bytes of this
     * resource. Cannot return null.
     * @throws IOException If the underlying resource is not found, or cannot be
     * accessed or any other problem with creating a valid InputStream.
     */
    InputStream inputStream() throws IOException;
}
