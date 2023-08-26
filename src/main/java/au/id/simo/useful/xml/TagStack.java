package au.id.simo.useful.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Entry point for SAX based XML parsing using a Tag Stack. A more convenient way to
 * use the {@link TagStackSaxHandler}.
 * <p>
 * It offers a lower amount of boilerplate code than using a SAX parser directly,
 * but is almost as memory efficient.
 * <p>
 * Assuming xml file {@code example.xml}:
 * <pre>
 *  &lt;document&gt;
 *      &lt;section&gt;
 *          &lt;text&gt;This is some text&lt;/text&gt;
 *      &lt;/section&gt;
 *  &lt;/document&gt;
 * </pre>
 * Usage:
 * <pre>
 * TagStack.parse(new FileInputStream("example.xml"), (tag) -> {
 *   System.out.println(tag.getXPath());
 * });
 * </pre>
 * The {@link TagHandler} lambda is called when the xml tags are closed, so the output would be:
 * <pre>
 *  /document/section/text
 *  /document/section
 *  /document
 * </pre>
 * @see TagStackSaxHandler
 */
public class TagStack {

    private TagStack(){}

    /**
     * Will close the provided input stream.
     *
     * @param inputStream The InputStream to read the XML from.
     * @throws IOException If there is an issue in reading from the InputStream.
     * @throws SAXException If there is a syntax issue in reading the XML.
     * @throws ParserConfigurationException If there is an issue with the
     * configuration of the XML parser.
     */
    public static void parse(InputStream inputStream, TagHandler tagHandler) throws IOException, SAXException, ParserConfigurationException {
        try (InputStream in = inputStream) {
            parse(new InputSource(in), tagHandler);
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
    public static void parse(Reader reader, TagHandler tagHandler) throws IOException, SAXException, ParserConfigurationException {
        try (Reader r = reader) {
            parse(new InputSource(r), tagHandler);
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
    public static void parse(InputSource inputSource, TagHandler tagHandler) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new TagStackSaxHandler(tagHandler));
        xmlReader.parse(inputSource);
    }
}
