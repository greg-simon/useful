package au.id.simo.useful.io;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class StringResourceTest implements ResourceTest {

    @Override
    public Resource createResource(byte[] testData, Charset charset) throws IOException {
        return new StringResource(new String(testData,charset), charset);
    }
}
