package au.id.simo.useful.xml;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Custom implementation of a SAX DefaultHandler, to make traversing an XML
 * document easier.
 *
 * @see TagStack
 */
public class TagStackSaxHandler extends DefaultHandler {

    private final Deque<Tag> tagStack;
    private final Map<String, String> prefixMappings;

    private final TagHandler tagHandler;

    protected Tag currentTag;

    public TagStackSaxHandler(TagHandler tagHandler) {
        this.tagStack = new ArrayDeque<>();
        this.prefixMappings = new LinkedHashMap<>();
        this.tagHandler = tagHandler;
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

        tagHandler.startTag(currentTag);
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
        tagHandler.endTag(currentTag);

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
}
