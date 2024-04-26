package au.id.simo.useful.io.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import au.id.simo.useful.io.StringResource;
import au.id.simo.useful.io.URLSession;
import au.id.simo.useful.io.URLSessionTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LocalURLConnectionTest {

    @BeforeAll
    public static void init() {
        Handler.registerHandlerIfRequired();
    }

    @Test
    public void testConnect() throws Exception {
        try (URLSession session = LocalProtocol.newSession()) {
            String urlStr = session.register("path", new StringResource("contents"));

            URL url = URI.create(urlStr).toURL();
            URLConnection connection = url.openConnection();
            assertInstanceOf(LocalURLConnection.class, connection);
            connection.connect();
            assertTrue(((LocalURLConnection) connection).isConnected());
        }
    }

    @Test
    public void testGetInputStream() throws Exception {
        try (URLSession session = LocalProtocol.newSession()) {
            String urlStr = session.register("path", new StringResource("contents"));

            URL url = URI.create(urlStr).toURL();
            URLConnection connection = url.openConnection();
            assertInstanceOf(LocalURLConnection.class, connection);
            assertFalse(((LocalURLConnection) connection).isConnected());

            // read contents twice
            for (int i = 0; i < 2; i++) {
                InputStream in = connection.getInputStream();
                assertTrue(((LocalURLConnection) connection).isConnected());
                String contents = new String(URLSessionTest.readAllBytesAndClose(in));
                assertEquals("contents", contents);
            }
        }
    }
    
    @Test
    public void testGetInputStream_Unknown_Session() throws Exception {
        // ensuring the local protocol is registered as we're relying on it first via URI and not via LocalProtocol class
        Handler.registerHandlerIfRequired();
        System.out.println(Thread.currentThread().getName());
        System.out.println(System.getProperty("java.protocol.handler.pkgs", ""));
        URL url = URI.create("local://unknownsession/path").toURL();
        URLConnection connection = url.openConnection();
        assertInstanceOf(LocalURLConnection.class, connection);
        assertFalse(((LocalURLConnection) connection).isConnected());
        
        IOException ioe = assertThrows(IOException.class, connection::getInputStream);
        assertEquals("Unknown local session (Session may have been closed): unknownsession", ioe.getMessage());
    }
}
