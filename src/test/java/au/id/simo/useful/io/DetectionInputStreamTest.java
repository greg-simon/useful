package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import au.id.simo.useful.io.DetectionInputStream.MatchListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class DetectionInputStreamTest {

    @Test
    public void testRead() throws Exception {
        runTest("1234567890", "12345", "67890");
        runTest("Hello there", "Hello", " there");
        runTest("[sudo] password: FURTHER OUTPUT", "[sudo] password: ", "FURTHER OUTPUT");
        runTest("blah blah match in the middle blah blah blah", "match in the middle", "blah blah  blah blah blah");
    }
    
    private void runTest(String testStr, String detectStr, String remainder) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(testStr.getBytes());
        MatchAction matchListener = new MatchAction();
        DetectionInputStream din = new DetectionInputStream(bin, detectStr.getBytes(), matchListener);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        int byt;
        while((byt=din.read())!=-1) {
            bout.write(byt);
        }
        assertTrue(matchListener.matched);
        assertEquals(remainder, bout.toString());
    }
    
    private class MatchAction implements MatchListener {
        boolean matched = false;
        @Override
        public void match(byte[] detected) throws IOException {
            matched = true;
        }
    }
}
