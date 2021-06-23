package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import au.id.simo.useful.Cleaner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class HashInputStreamTest {

    public HashInputStreamTest() {
    }
    
    private static final Resource TEST_DATA = () -> {
        return new ByteArrayInputStream(
                "This is some text that will be hashed.".getBytes("UTF-8")
        );
    };
    
    /**
     * Compares output with linux based sum programs. E.g md5sum, sha256sum
     * @param sumCmd
     * @param hashAlgo
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException 
     */
    @EnabledOnOs(OS.LINUX)
    @ParameterizedTest
    @MethodSource("linuxHashComparison")
    public void compareHashOnLinux(String sumCmd, String hashAlgo) throws IOException, NoSuchAlgorithmException, InterruptedException {
        Process sumProcess = new ProcessBuilder().command(sumCmd).start();
        String javaHash;
        String processHash;
        try (Cleaner c = new Cleaner()) {
            OutputStream out = c.closeOnClean(sumProcess.getOutputStream());
            HashInputStream hin = c.closeOnClean(new HashInputStream(TEST_DATA.inputStream(),hashAlgo));
            Resource.copy(hin, out);
            javaHash = hin.getHashString();
        }
        
        try (InputStream in = sumProcess.getInputStream()) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Resource.copy(in, bout);
            // string is formatted as "<hash> -"
            processHash = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            int spaceIndex = processHash.indexOf(" ");
            if (spaceIndex > 0) {
                processHash = processHash.substring(0, spaceIndex);
            }
        }
        sumProcess.waitFor();
        assertEquals(javaHash, processHash);
    }
    
    static Stream<Arguments> linuxHashComparison() {
        return Stream.of(
                Arguments.of("md5sum", "MD5"),
                Arguments.of("sha256sum", "SHA-256"),
                Arguments.of("sha512sum", "SHA-512")
        );
    }
    
    @Test
    public void testReadMD5_readArray() throws Exception {
        InputStream in = TEST_DATA.inputStream();
        HashInputStream hin = new HashInputStream(in, "MD5");
        // available() should return count of all bytes from underlying bytearray stream
        byte[] buffer = new byte[hin.available()];
        int read = hin.read(buffer);
        hin.close();
        assertEquals(read, buffer.length);
        assertEquals("ae9626dfbb2fffc0daa201e7bc032ba1", hin.getHashString());
    }
    
    @Test
    public void testReadMD5() throws Exception {
        InputStream in = TEST_DATA.inputStream();
        HashInputStream hin = new HashInputStream(in, "MD5");
        while(hin.read()!=-1){} // read through the whole input stream
        hin.close();
        
        assertEquals("ae9626dfbb2fffc0daa201e7bc032ba1", hin.getHashString());
    }
    
    @Test
    public void testReadSHA512() throws Exception {
        InputStream in = TEST_DATA.inputStream();
        HashInputStream hin = new HashInputStream(in, "SHA-512");
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
