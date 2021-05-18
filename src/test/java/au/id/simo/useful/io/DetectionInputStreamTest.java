package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.id.simo.useful.io.DetectionInputStream.Match;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import au.id.simo.useful.io.DetectionInputStream.OnMatch;

/**
 *
 */
public class DetectionInputStreamTest implements FilterInputStreamTest {

    @Override
    public FilterInputStream create(InputStream in) {
        return new DetectionInputStream(in);
    }
    
    @Test
    public void testRead() throws Exception {
        runTest("1234567890", "12345", 1, "67890");
        runTest("Hello there", "Hello", 1, " there");
        runTest("[sudo] password: FURTHER OUTPUT", "[sudo] password: ", 1, "FURTHER OUTPUT");
        runTest(" match in the middle blah blah blah", "match in the middle", 1, "  blah blah blah");
        runTest("blah blah match in the middle blah blah blah", "match in the middle", 1, "blah blah  blah blah blah");
        runTest(" A ", " ", 2, "A");
        runTest("blah blah blah", " ", 2, "blahblahblah");
        runTest("AAAAA", "A", 5, "");
        runTest("There once was a boy named Roy, who lived in a toy", "oy", 3, "There once was a b named R, who lived in a t");
    }
    
    private void runTest(String testStr, String detectStr, int expectedMatchCount,  String remainder) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(testStr.getBytes());
        FilterOnMatch matchListener = new FilterOnMatch();
        Match match = new Match(detectStr.getBytes(), matchListener);
        DetectionInputStream din = new DetectionInputStream(bin, match);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        int byt;
        while((byt=din.read())!=-1) {
            bout.write(byt);
        }
        assertEquals(expectedMatchCount, matchListener.matchCount);
        assertEquals(remainder, bout.toString());
    }

    
    private class FilterOnMatch implements OnMatch {
        int matchCount = 0;
        @Override
        public boolean filter(byte[] detected) throws IOException {
            matchCount++;
            return true;
        }
    }
}
