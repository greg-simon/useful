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
 * assume UTF-8 as the character encoding.
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
            copy(in, bout);
            return bout.toByteArray();
        }
    }

    /**
     * Copies all the contents from the given input stream to the given output
     * stream.
     *
     * @param input the input stream
     * @param output the output stream
     * @return the number of bytes that have been copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * A convenience method that copies bytes from this instance's InputStream
     * to the OutputStream provided.
     * <p>
     * NOTE: From Java 9+ this will be redundant due to
     * {@code InputStream.transferTo(OutputStream)}
     *
     * @param out Underlying bytes this resources refers to is written to this
     * OutputStream
     * @return the number of bytes written to the provided OutputStream
     * @throws IOException if there was an issue in reading from this resource's
     * bytes, or writing to the provided OutputStream
     */
    default long copyTo(OutputStream out) throws IOException {
        try (InputStream in = inputStream()) {
            return copy(in, out);
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
