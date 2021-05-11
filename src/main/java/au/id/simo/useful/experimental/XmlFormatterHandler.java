package au.id.simo.useful.experimental;

import java.io.IOException;
import java.io.Writer;

import au.id.simo.useful.xml.StringUtil;
import au.id.simo.useful.xml.Tag;
import au.id.simo.useful.xml.XmlHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 */
public class XmlFormatterHandler extends XmlHandler {
    private static final String INDENT_STR = "  ";
    protected final Writer output;

    public XmlFormatterHandler(Writer output) {
        this.output = output;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try {
            output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            output.write(System.lineSeparator());
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        writeTagStart(currentTag);
    }

    protected void writeTagStart(Tag tag) throws SAXException {
        try {
            if (tag.hasParent() && !tag.getParent().hasSignificantContent()) {
                output.write(System.lineSeparator());
                writeIndentStr(output);
            }
            output.write("<");
            output.write(tag.getName());
            for (String tagName : tag.keySet()) {
                output.write(" ");
                output.write(tagName);
                output.write("=\'");
                output.write(StringUtil.escapeAttribute(tag.get(tagName)));
                output.write("\'");
            }
            output.write(">");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        writeTagEnd(currentTag);
        super.endElement(uri, localName, qName);
    }

    protected void writeTagEnd(Tag tag) throws SAXException {
        try {
            if (tag.hasSignificantContent()) {
                output.write(StringUtil.escapeContent(tag.getContent()));
            } else if (tag.hasChildren()) {
                output.write(System.lineSeparator());
                writeIndentStr(output);
            }
            output.write("</");
            output.write(tag.getName());
            output.write(">");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            output.write(System.lineSeparator());
            output.flush();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    private void writeIndentStr(Writer out) throws IOException {
        int tagStackSize = this.tagStack.size();
        for (int i = 0; i < tagStackSize; i++) {
            out.write(INDENT_STR);
        }
    }

    @Override
    public void endTag(Tag tag) {
        // no op
    }
}
