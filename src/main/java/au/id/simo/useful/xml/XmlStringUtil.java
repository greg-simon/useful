package au.id.simo.useful.xml;

/**
 * String Utilities for modifying String and escaping XML characters.
 */
public class XmlStringUtil {

    private XmlStringUtil() {
        // no-op
    }
    
    public static String escapeAttribute(String str) {
        StringBuilder sb = new StringBuilder(str);
        replaceAttribute(sb);
        return sb.toString();
    }

    public static String escapeContent(String str) {
        StringBuilder sb = new StringBuilder(str);
        replaceContent(sb);
        return sb.toString();
    }

    public static void replaceContent(StringBuilder sb) {
        replace(sb, "&", "&amp;");
        replace(sb, "<", "&lt;");
        replace(sb, ">", "&gt;");
    }

    public static void replaceAttribute(StringBuilder sb) {
        replaceContent(sb);
        replace(sb, "'", "&apos;");
        replace(sb, "\"", "&quot;");
        replace(sb, "\t", "&#x9;");
        replace(sb, "\n", "&#xA;");
        replace(sb, "\r", "&#xD;");
    }

    public static void replace(StringBuilder sb, String find, String replaceWith) {
        int index = 0;
        while ((index = sb.indexOf(find, index)) != -1) {
            sb.replace(index, index + find.length(), replaceWith);
            index++;
        }
    }
}
