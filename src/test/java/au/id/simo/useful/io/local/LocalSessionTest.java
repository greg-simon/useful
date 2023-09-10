package au.id.simo.useful.io.local;

import au.id.simo.useful.io.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class LocalSessionTest implements URLSessionTest {

    @AfterEach
    public void verifyAllSessionsAreClosed() {
        //assertEquals(0, LocalProtocol.sessionCount());
    }

    @Override
    public URLSession createURLSession() throws IOException {
        return LocalProtocol.newSession();
    }

    @Test
    public void testBasicUsage() throws IOException {
        try (URLSession session = LocalProtocol.newSession()) {
            String baseUrlStr = session.getBaseUrl();
            URL baseUrl = URI.create(baseUrlStr).toURL();
            String sessionId = baseUrl.getHost();

            String genUrl = session.register("generator", new Generator() {
                @Override
                public void writeTo(OutputStream out) throws IOException {
                    out.write("This is a generator".getBytes());
                }
            });
            String resUrl = session.register("resource", new Resource() {
                @Override
                public InputStream inputStream() throws IOException {
                    return new ByteArrayInputStream("This is a resource".getBytes());
                }
            });

            String expectedGenUrl = "local://" + sessionId + "/generator";
            String expectedResUrl = "local://" + sessionId + "/resource";
            assertEquals(expectedGenUrl, genUrl);
            assertEquals(expectedResUrl, resUrl);

            assertEquals("This is a generator", urlToString(genUrl));
            assertEquals("This is a resource", urlToString(resUrl));

            assertEquals("This is a generator", IOUtils.getStringAsUTF8(session.getResource("generator")));
            assertEquals("This is a resource", IOUtils.getStringAsUTF8(session.getResource("resource")));
        }
    }

    public static String urlToString(String urlStr) throws IOException {
        return IOUtils.getStringAsUTF8(new URLResource(urlStr));
    }
}
