package au.id.simo.useful.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Custom implementation of a SAX DefaultHandler, to make traversing an XML
 * document easier.
 * <p>
 * To use, extend this class and implement the endTag method.
 */
public abstract class XmlHandler extends DefaultHandler {

    protected final Deque<Tag> tagStack;
    protected final Map<String, String> prefixMappings;

    protected Tag currentTag;

    protected XmlHandler() {
        this.tagStack = new ArrayDeque<>();
        this.prefixMappings = new LinkedHashMap<>();
    }

    /**
     * This method is called when the provided tag has closed, and the fully
     * constructed {@link Tag} object is passed in as an argument.
     *
     * @param tag The tag that has closed in the endElement SAX event.
     */
    public abstract void endTag(Tag tag);

    /**
     * Run just after an opening tag has been read.
     *
     * @param tag the opening tag. It will not contain any text as text will not
     * have been read yet. But it will contain any tag attributes present.
     */
    public void startTag(Tag tag) {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMappings.put("xmlns:" + prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Tag parent = null;
        if (currentTag != null) {
            parent = currentTag;
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

        startTag(currentTag);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentTag == null) {
            // mostly likely the parser has already thrown the exception and this
            // code will never be reached.
            throw new SAXException("Should not receive characters outside a tag.");
        }
        currentTag.appendContent(ch, start, length);
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
     * @param inputStream The InputStream to read the XML from.
     * @throws IOException If there is an issue in reading from the InputStream.
     * @throws SAXException If there is a syntax issue in reading the XML.
     * @throws ParserConfigurationException If there is an issue with the
     * configuration of the XML parser.
     */
    public void parse(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        try (InputStream in = inputStream) {
            parse(new InputSource(in));
        }
    }
    
    /**
     * Will close the provided reader.
     *
     * @param reader The Reader to read the XML from.
     * @throws IOException If there is an issue in reading from the InputStream.
     * @throws SAXException If there is a syntax issue in reading the XML.
     * @throws ParserConfigurationException If there is an issue with the
     * configuration of the XML parser.
     */
    public void parse(Reader reader) throws IOException, SAXException, ParserConfigurationException {
        try (Reader r = reader) {
            parse(new InputSource(r));
        }
    }
    
    /**
     * Will not close any InputSource, the caller must do that.
     * 
     * @param inputSource The InputSource to read the XML from.
     * @throws IOException If there is an issue in reading from the InputStream.
     * @throws SAXException If there is a syntax issue in reading the XML.
     * @throws ParserConfigurationException If there is an issue with the
     * configuration of the XML parser.
     */
    public void parse(InputSource inputSource) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse(inputSource);
    }
}
