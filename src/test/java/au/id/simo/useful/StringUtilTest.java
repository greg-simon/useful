package au.id.simo.useful;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class StringUtilTest {

    @Test
    public void testEscapeSimple() {
        String str = "Hello & Goodbye";
        assertEquals("Hello &amp; Goodbye", StringUtil.escapeAttribute(str));
    }
    
    @Test
    public void testEscapeAllAtOnce() {
        String str = "' \" \t \n \r \r\n & < >";
        assertEquals("&apos; &quot; &#x9; &#xA; &#xD; &#xD;&#xA; &amp; &lt; &gt;", StringUtil.escapeAttribute(str));
    }
    
    @Test
    public void testEscapeContent() {
        String str = "& < >";
        assertEquals("&amp; &lt; &gt;", StringUtil.escapeContent(str));
    }
}
