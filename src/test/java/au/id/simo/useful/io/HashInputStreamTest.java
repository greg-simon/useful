package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author gsimon
 */
public class HashInputStreamTest {

    public HashInputStreamTest() {
    }

    /**
     * Test of read method, of class HashInputStream.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testRead() throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(
                "This is some text that will be hashed.".getBytes("UTF-8")
        );
        HashInputStream hin = new HashInputStream(bin, "MD5");

        while(hin.read()!=-1){} // read through the whole input stream
        hin.close();
        
        assertEquals("ae9626dfbb2fffc0daa201e7bc032ba1", hin.getHashString());
    }
}
