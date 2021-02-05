package au.id.simo.useful.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author gsimon
 */
public class DataGenFactoryTest {
    
    public DataGenFactoryTest() {
    }

    @Test
    public void testIncrementingBytes() throws IOException {
        InputStream in = DataGenFactory.incrementingBytes();
        for (int i=0;i<300;i++) {
            int expected = i % 256;
            assertEquals(expected, in.read());
        }
    }
    
}