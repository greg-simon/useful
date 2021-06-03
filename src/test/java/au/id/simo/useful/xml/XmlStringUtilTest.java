package au.id.simo.useful.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class XmlStringUtilTest {

    @Test
    public void testEscapeSimple() {
        String str = "Hello & Goodbye";
        assertEquals("Hello &amp; Goodbye", XmlStringUtil.escapeAttribute(str));
    }

    @Test
    public void testEscapeAllAtOnce() {
        String str = "' \" \t \n \r \r\n & < >";
        assertEquals("&apos; &quot; &#x9; &#xA; &#xD; &#xD;&#xA; &amp; &lt; &gt;", XmlStringUtil.escapeAttribute(str));
    }

    @Test
    public void testEscapeContent() {
        String str = "& < >";
        assertEquals("&amp; &lt; &gt;", XmlStringUtil.escapeContent(str));
    }
}
