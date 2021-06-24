package au.id.simo.useful.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class XmlHandlerTest {
    public static final String TEST_DOC = ""
            + "\n\n   \t\t  <doc>\n"
            + "  <onelevel level=\"1\">\n"
            + "    <twolevel level=\"2\">\n"
            + "      <text>This is text</text>\n"
            + "    </twolevel>\n"
            + "  </onelevel>\n"
            + "</doc>"
            ;
    
    public static InputStream testStream() throws IOException {
        return new ByteArrayInputStream(TEST_DOC.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testXmlHandler() throws IOException, SAXException, ParserConfigurationException {
        List<Tag> tags = new ArrayList<>();
        XmlHandler xmlHandler = new XmlHandler() {
            @Override
            public void endTag(Tag tag) {
                tags.add(tag);
            }
        };
        xmlHandler.parseStream(testStream());
        
        // test tag count and ordering
        assertEquals(4, tags.size());
        Tag textTag = tags.get(0);
        Tag twolevelTag = tags.get(1);
        Tag onelevelTag = tags.get(2);
        Tag docTag = tags.get(3);
        assertEquals("text", textTag.getName());
        assertEquals("twolevel", twolevelTag.getName());
        assertEquals("onelevel", onelevelTag.getName());
        assertEquals("doc", docTag.getName());
        
        // test attributes
        assertEquals("1", onelevelTag.get("level"));
        assertEquals("2", twolevelTag.get("level"));
        
        assertTrue(textTag.hasSignificantContent());
        
        assertEquals("This is text", textTag.getContent());
    }
}
