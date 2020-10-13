package au.id.simo.useful.io;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 *
 */
public class RewindWriterTest {

    @Test
    public void testMarkAndRewind() throws IOException {
        StringWriter out = new StringWriter();
        RewindWriter rout = new RewindWriter(out);
        
        rout.write("testing 123");
        rout.mark();
        rout.write("this shouldn't be written.");
        rout.rewind();
        rout.close();
        
        assertEquals("testing 123", out.toString());
    }
    
    @Test
    public void testBufferBust() throws IOException {
        StringWriter out = new StringWriter();
        RewindWriter rout = new RewindWriter(out, 10);
        
        rout.mark();
        rout.write("0123456789");
        assertTrue(rout.isMarked(),"Mark still valid");
        assertEquals(0, out.toString().length(), "Nothing yet written");
        
        // this will blow the buffer
        rout.write("-");
        assertFalse(rout.isMarked(), "Buffer now blown, mark cleared");
        assertEquals("0123456789-", out.toString(), "Blown buffer written out");
    }
    
    @Test
    public void testReMark() throws IOException {
        StringWriter out = new StringWriter();
        RewindWriter rout = new RewindWriter(out, 10);
        
        rout.mark();
        rout.write("12345");
        assertTrue(rout.isMarked(),"Mark still valid");
        assertEquals(0, out.toString().length(),"Nothing yet written");
        
        // this will write current buffer out and reset the mark
        rout.mark();
        assertTrue(rout.isMarked(), "mark  is still set");
        assertEquals("12345", out.toString(), "Old buffer written out");
    }
}
