package au.id.simo.useful.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used to write data to an {@link OutputStream}.
 * <p>
 * Implementations are to ensure the {@link #writeTo(java.io.OutputStream)}
 * method is safe to call multiple times to get the same output.
 */
public interface Generator {

    /**
     * Writes the generated output to the provided OutputStream.
     * <p>
     * Implementations of this method are to ensure it is safe to call this
     * method multiple times to get the same output.
     * <p>
     * Implementations are not to close the provided OutputStream, but should
     * call {@link OutputStream#flush()}.
     *
     * @param out The output stream to write the generated content to.
     * @throws IOException if there is any errors in writing to the provided
     * output stream, or in generating the content to write.
     */
    void writeTo(OutputStream out) throws IOException;
}
