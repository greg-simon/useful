package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import au.id.simo.useful.io.DetectionInputStream.Match;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import au.id.simo.useful.io.DetectionInputStream.OnMatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 */
public class DetectionInputStreamTest implements FilterInputStreamTest {

    @Override
    public FilterInputStream create(InputStream in) {
        return new DetectionInputStream(in);
    }
    
    
    Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("1234567890", "12345", 1, "67890"),
                Arguments.of("Hello there", "Hello", 1, " there"),
                Arguments.of("[sudo] password: FURTHER OUTPUT", "[sudo] password: ", 1, "FURTHER OUTPUT"),
                Arguments.of(" match in the middle blah blah blah", "match in the middle", 1, "  blah blah blah"),
                Arguments.of("blah blah match in the middle blah blah blah", "match in the middle", 1, "blah blah  blah blah blah"),
                Arguments.of(" A ", " ", 2, "A"),
                Arguments.of("blah blah blah", " ", 2, "blahblahblah"),
                Arguments.of("AAAAA", "A", 5, ""),
                Arguments.of("There once was a boy named Roy, who lived in a toy", "oy", 3, "There once was a b named R, who lived in a t")
        );
    }
    
    @ParameterizedTest
    @MethodSource("testSource")
    public void testRead(String testStr, String detectStr, int expectedMatchCount,  String remainder) throws Exception {
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
    
    @ParameterizedTest
    @MethodSource("testSource")
    public void testReadArray(String testStr, String detectStr, int expectedMatchCount,  String remainder) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(testStr.getBytes());
        FilterOnMatch matchListener = new FilterOnMatch();
        Match match = new Match(detectStr.getBytes(), matchListener);
        DetectionInputStream din = new DetectionInputStream(bin, match);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        int readCount;
        byte[] buf = new byte[5];
        while((readCount=din.read(buf))!=-1) {
            bout.write(buf, 0, readCount);
        }
        assertEquals(expectedMatchCount, matchListener.matchCount, "Verify match count");
        assertEquals(remainder, bout.toString(),"Verify expected output");
    }
    
    @ParameterizedTest
    @MethodSource("testSource")
    public void testReadArray3Args(String testStr, String detectStr, int expectedMatchCount,  String remainder) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(testStr.getBytes());
        FilterOnMatch matchListener = new FilterOnMatch();
        Match match = new Match(detectStr.getBytes(), matchListener);
        DetectionInputStream din = new DetectionInputStream(bin, match);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        int readCount;
        byte[] buf = new byte[5];
        while((readCount=din.read(buf, 0, buf.length))!=-1) {
            bout.write(buf, 0, readCount);
        }
        assertEquals(expectedMatchCount, matchListener.matchCount, "Verify match count");
        assertEquals(remainder, bout.toString(),"Verify expected output");
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
