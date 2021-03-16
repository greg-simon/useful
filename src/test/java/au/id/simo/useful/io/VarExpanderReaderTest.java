package au.id.simo.useful.io;


import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class VarExpanderReaderTest {

    @Test
    public void testHappyReadArray() throws Exception {
        Map<String,String> varMap = new HashMap<>();
        varMap.put("NAME", "John Doe");
        Reader in = new StringReader("Hello ${NAME}");
        VarExpanderReader reader = new VarExpanderReader(in, varMap, "${", "}");
        char[] output = new char[1024];
        int readCount = reader.read(output);
        String outputStr = new String(output, 0, readCount);
        assertEquals("Hello John Doe", outputStr);
    }
    
    @Test
    public void testHappyReadArrayNoSuffix() throws Exception {
        Map<String,String> varMap = new HashMap<>();
        varMap.put("NAME", "John Doe");
        Reader in = new StringReader("Hello $NAME");
        VarExpanderReader reader = new VarExpanderReader(in, varMap, "$", null);
        char[] output = new char[1024];
        int readCount = reader.read(output);
        String outputStr = new String(output, 0, readCount);
        assertEquals("Hello John Doe", outputStr);
    }
    
    @Test
    public void testHappyRead() throws Exception {
        Map<String,String> varMap = new HashMap<>();
        varMap.put("NAME", "John Doe");
        Reader in = new StringReader("Hello ${NAME}");
        VarExpanderReader reader = new VarExpanderReader(in, varMap, "${", "}");
        StringBuilder sb = new StringBuilder();
        int i;
        while((i=reader.read())!=-1) {
            sb.append((char)i);
        }
        String outputStr = sb.toString();
        assertEquals("Hello John Doe", outputStr);
    }
    
    @Test
    public void testHappyReadNoSuffix() throws Exception {
        Map<String,String> varMap = new HashMap<>();
        varMap.put("NAME", "John Doe");
        Reader in = new StringReader("Hello $NAME");
        VarExpanderReader reader = new VarExpanderReader(in, varMap, "$", null);
        StringBuilder sb = new StringBuilder();
        int i;
        while((i=reader.read())!=-1) {
            sb.append((char)i);
        }
        String outputStr = sb.toString();
        assertEquals("Hello John Doe", outputStr);
    }
}
