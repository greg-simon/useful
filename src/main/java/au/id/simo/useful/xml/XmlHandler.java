package au.id.simo.useful.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Custom implementation of a SAX DefaultHandler, to make traversing an xml
 * document easier.
 *
 * To use, extend this class and implement the endTag method.
 */
public abstract class XmlHandler extends DefaultHandler {

    private final Deque<Tag> tagStack;
    private final Map<String, String> prefixMappings;

    private Tag currentTag;

    public XmlHandler() {
        this.tagStack = new LinkedList<>();
        this.prefixMappings = new LinkedHashMap<>();
    }

    public Tag getCurrentTag() {
        return currentTag;
    }

    public Deque<Tag> getTagStack() {
        return new LinkedList<>(tagStack);
    }

    /**
     * This method is called when the provided tag has closed, and the fully
     * constructed {@link Tag} object is passed in as an argument.
     *
     * @param tag The tag that has closed in the endElement SAX event.
     */
    public abstract void endTag(Tag tag);

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMappings.put("xmlns:" + prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Tag parent = null;
        if (currentTag != null) {
            parent = currentTag;
            parent.setHadChild(true);
            tagStack.push(currentTag);
        }
        currentTag = new Tag(qName, parent);

        // add in prefix mappings if they exist
        if (!prefixMappings.isEmpty()) {
            currentTag.putAll(prefixMappings);
            prefixMappings.clear();
        }

        // attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            currentTag.put(attributes.getQName(i), attributes.getValue(i));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentTag != null) {
            currentTag.appendContent(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        endTag(currentTag);

        if (tagStack.isEmpty()) {
            currentTag = null;
            return;
        }

        currentTag = tagStack.pop();
    }

    @Override
    public void endDocument() throws SAXException {
        this.currentTag = null;
        this.prefixMappings.clear();
        this.tagStack.clear();
    }
    

    /**
     * Will close the provided input stream.
     *
     * @param inputStream
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void parseStream(InputStream inputStream) throws IOException,
            SAXException, ParserConfigurationException {
        try (InputStream in = inputStream) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.parse(new InputSource(in));
        }
    }
}
