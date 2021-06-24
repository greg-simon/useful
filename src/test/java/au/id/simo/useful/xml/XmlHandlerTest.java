package au.id.simo.useful.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import au.id.simo.useful.io.CloseStatus;
import au.id.simo.useful.io.StringResource;
import au.id.simo.useful.io.URLSession;
import au.id.simo.useful.io.local.LocalProtocol;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
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

    @Test
    public void testParseInputSourceUrl() throws IOException, SAXException, ParserConfigurationException {
        TagListXmlHandler xmlHandler = new TagListXmlHandler();
        try (URLSession session = LocalProtocol.newSession()) {
            String url = session.register("doc", new StringResource(TEST_DOC));
            xmlHandler.parse(new InputSource(url));
        }
        
        testTags(xmlHandler.tagList);
    }
    
    @Test
    public void testParseInputStream() throws IOException, SAXException, ParserConfigurationException {
        TagListXmlHandler xmlHandler = new TagListXmlHandler();
        CloseStatus cs = new CloseStatus();
        ByteArrayInputStream in = new ByteArrayInputStream(TEST_DOC.getBytes()) {
            @Override
            public void close() throws IOException {
                super.close();
                cs.close();
            }
        };
        xmlHandler.parse(in);
        testTags(xmlHandler.tagList);
        assertTrue(cs.isClosed());
    }
    
    @Test
    public void testParseReader() throws IOException, SAXException, ParserConfigurationException {
        TagListXmlHandler xmlHandler = new TagListXmlHandler();
        CloseStatus cs = new CloseStatus();
        Reader in = new StringReader(TEST_DOC) {
            @Override
            public void close() {
                super.close();
                cs.close();
            }
        };
        xmlHandler.parse(in);
        testTags(xmlHandler.tagList);
        assertTrue(cs.isClosed());
    }

    private void testTags(List<Tag> tags) {
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
    
    private class TagListXmlHandler extends XmlHandler {
        List<Tag> tagList = new ArrayList<>();
        
        @Override
        public void endTag(Tag tag) {
            tagList.add(tag);
        }
    }
}
