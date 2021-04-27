package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class HashInputStreamTest {

    public HashInputStreamTest() {
    }
    
    @Test
    public void testReadMD5_readArray() throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(
                "This is some text that will be hashed.".getBytes("UTF-8")
        );
        HashInputStream hin = new HashInputStream(bin, "MD5");
        // available() should return count of all bytes from underlying bytearray stream
        byte[] buffer = new byte[hin.available()];
        int read = hin.read(buffer);
        hin.close();
        assertEquals(read, buffer.length);
        assertEquals("ae9626dfbb2fffc0daa201e7bc032ba1", hin.getHashString());
    }
    
    @Test
    public void testReadMD5() throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(
                "This is some text that will be hashed.".getBytes("UTF-8")
        );
        HashInputStream hin = new HashInputStream(bin, "MD5");
        while(hin.read()!=-1){} // read through the whole input stream
        hin.close();
        
        assertEquals("ae9626dfbb2fffc0daa201e7bc032ba1", hin.getHashString());
    }
    
    @Test
    public void testReadSHA512() throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(
                "This is some text that will be hashed.".getBytes("UTF-8")
        );
        HashInputStream hin = new HashInputStream(bin, "SHA-512");
        while(hin.read()!=-1){} // read through the whole input stream
        hin.close();
        
        assertEquals(
                "ff8c8247170a08bb4947d915bbe2ee199c7305498065287f99bc36307185a8b4f674eb0b90c571ae80c6acb8a6c5e4e1b0cb22e4315cb6792922070ef96f8bcd",
                hin.getHashString()
        );
    }
    
    @Test
    public void testRead_InvalidAlgo() throws Exception {
        assertThrows(NoSuchAlgorithmException.class, () -> {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
            HashInputStream hin = new HashInputStream(byteArrayInputStream, "Nonsense algo here");
        });
    }
}
