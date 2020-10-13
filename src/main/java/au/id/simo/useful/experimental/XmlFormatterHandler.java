package au.id.simo.useful.experimental;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import au.id.simo.useful.StringUtil;
import au.id.simo.useful.xml.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class XmlFormatterHandler extends DefaultHandler {
    private static final String INDENT_STR = "  ";
    
    protected final Writer output;
    protected final Stack<Tag> tagStack;
    protected Tag currentTag;
    
    private final Map<String,String> prefixMappings;
    
    public XmlFormatterHandler(Writer output) {
        this.output = output;
        this.tagStack = new Stack<>();
        this.prefixMappings = new LinkedHashMap<>();
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            output.write(System.lineSeparator());
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
    
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMappings.put("xmlns:"+prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Tag parent = null;
        if(currentTag!=null) {
            parent = currentTag;
            tagStack.push(currentTag);
        }
        currentTag = new Tag(qName, parent);
        
        // add in prefix mappings if they exist
        if(!prefixMappings.isEmpty()) {
            currentTag.putAll(prefixMappings);
            prefixMappings.clear();
        }
        
        // attributes
        for(int i=0;i<attributes.getLength();i++) {
            currentTag.put(attributes.getQName(i), attributes.getValue(i));
        }
        
        writeTagStart(currentTag);
    }
    
    protected void writeTagStart(Tag tag) throws SAXException {
        try {
            if(tag.hasParent() && !tag.getParent().hasSignificantContent()) {
                output.write(System.lineSeparator());
                writeIndentStr(output);
            }
            output.write("<");
            output.write(tag.getName());
            for(String tagName: tag.keySet()) {
                output.write(" ");
                output.write(tagName);
                output.write("=\'");
                output.write(StringUtil.escapeAttribute(tag.get(tagName)));
                output.write("\'");
            }
            output.write(">");
        } catch(IOException e) {
            throw new SAXException(e);
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(currentTag != null) {
            currentTag.appendContent(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.characters(ch, start, length);
    }
    

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        writeTagEnd(currentTag);
        
        if(tagStack.size()>0) {
            currentTag = tagStack.pop();
        } else {
            currentTag = null;
        }
    }
    
    protected void writeTagEnd(Tag tag) throws SAXException {
        try {
            if(tag.hasSignificantContent()) {
                output.write(StringUtil.escapeContent(tag.getContent()));
            } else if(tag.hasChildren()) {
                output.write(System.lineSeparator());
                writeIndentStr(output);
            }
            output.write("</");
            output.write(tag.getName());
            output.write(">");
        } catch(IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.write(System.lineSeparator());
            output.flush();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
    
    private void writeIndentStr(Writer out) throws IOException {
        for(Tag t: tagStack) {
            out.write(INDENT_STR);
        }
    }
}
