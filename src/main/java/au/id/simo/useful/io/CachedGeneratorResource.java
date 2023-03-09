package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Turns an OutputStream into an InputStream by writing it all into an in memory
 * cache, then providing an InputStream for that cached data.
 * <p>
 * The usual byte array size limitation of 2GB for the Generator output applies.
 */
public class CachedGeneratorResource implements Resource {

    private final Generator generator;
    private byte[] generatedData;

    /**
     * Creates a new Resource that obtains its data from the provided
     * Generator.
     *
     * @param generator Is used to generate the data that backs this resource
     * implementation.
     */
    public CachedGeneratorResource(Generator generator) {
        this.generator = generator;
    }

    /**
     * Has the backing Generator write everything to an in memory cache, if not
     * done so already, before making the buffer available for reading via a new
     * InputStream.
     * <p>
     * The generator is only run once.
     *
     * @return An InputStream to read the in memory cache.
     * @throws IOException Will never be thrown for this implementation.
     */
    @Override
    public InputStream inputStream() throws IOException {
        if (generatedData == null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            generator.writeTo(bout);
            generatedData = bout.toByteArray();
        }
        return new ByteArrayInputStream(generatedData);
    }
}
