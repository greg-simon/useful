package au.id.simo.useful.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import au.id.simo.useful.io.local.LocalSessionTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the interface contracts and default methods.
 * <p>
 * Inherit this interface on your test class when testing
 * URLSession implementations.
 * @see FileSessionTest
 * @see LocalSessionTest
 */
@TestInstance(Lifecycle.PER_CLASS)
public interface URLSessionTest {

    URLSession createURLSession() throws IOException;
    
    /**
     * Replace when moving to java 9+
     * @param in
     * @return
     * @throws IOException 
     */
    static byte[] readAllBytesAndClose(InputStream in) throws IOException {
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4048];
            int readCount;
            while ((readCount = is.read(buf)) != -1) {
                out.write(buf, 0, readCount);
            }
            return out.toByteArray();
        }
    }
    
    /**
     * Replace with Files.writeString() when moving to java 11+
     * @param path
     * @param csq
     * @throws IOException 
     */
    static void writeString(Path path, CharSequence csq) throws IOException {
        Files.write(path, String.valueOf(csq).getBytes());
    }

    @Test
    default void testGetInputStream() throws Exception {
        Resource res = new StringResource("Test resource");
        byte[] fromRes = IOUtils.getBytes(res);

        try (URLSession sess = createURLSession()) {
            sess.register("test", res);

            InputStream in = sess.getInputStream("test");
            byte[] fromStream = readAllBytesAndClose(in);
            assertArrayEquals(fromRes, fromStream);
        }
    }

    @Test
    default void testGetInputStream_SessionClosed() throws Exception {
        Resource res = new StringResource("Test resource");

        URLSession sess = createURLSession();
        sess.register("test", res);
        sess.close();

        IOException ioe = assertThrows(IOException.class, () -> {
            sess.getInputStream("test");
        });
        assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
    }

    @Test
    default void testGetInputStream_ResourceNotFound() throws Exception {
        try (URLSession sess = createURLSession()) {
            // attempt to get inputstream from session with nothing registered.
            assertNull(sess.getInputStream("test"));
        }
    }

    @Test
    default void testForEachResource() throws Exception {
        try (URLSession sess = createURLSession()) {
            sess.register("path1", new StringResource("This is path1"));
            sess.register("path2", new StringResource("This is path2"));
            sess.register("path3", new StringResource("This is path3"));

            List<String> pathList = new ArrayList<>();
            List<String> resContentList = new ArrayList<>();
            sess.forEachResource((path, resource) -> {
                pathList.add(path);
                try {
                    resContentList.add(IOUtils.getStringAsUTF8(resource));
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            });

            assertEquals(3, pathList.size());
            assertEquals(3, resContentList.size());
            Collections.sort(pathList);
            assertEquals("path1", pathList.get(0), "list contains path1");
            assertEquals("path2", pathList.get(1), "list contains path2");
            assertEquals("path3", pathList.get(2), "list contains path3");
            assertTrue(resContentList.contains("This is path1"), "path1 contents found");
            assertTrue(resContentList.contains("This is path2"), "path2 contents found");
            assertTrue(resContentList.contains("This is path3"), "path3 contents found");
        }
    }

    @Test
    default void testForEachResource_NoResources() throws Exception {
        try (URLSession sess = createURLSession()) {
            List<String> pathList = new ArrayList<>();
            List<String> resContentList = new ArrayList<>();
            sess.forEachResource((path, resource) -> {
                pathList.add(path);
                try {
                    resContentList.add(IOUtils.getStringAsUTF8(resource));
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            });

            assertTrue(pathList.isEmpty());
            assertTrue(resContentList.isEmpty());
        }
    }

    @Test
    default void testForEachResource_ClosedSession() throws Exception {
        URLSession sess = createURLSession();
        sess.register("path1", new StringResource("This is path1"));
        sess.register("path2", new StringResource("This is path2"));
        sess.register("path3", new StringResource("This is path3"));
        sess.close();

        List<String> pathList = new ArrayList<>();
        List<String> resContentList = new ArrayList<>();

        IOException ioe = assertThrows(IOException.class, () -> {
            sess.forEachResource((path, resource) -> {
                pathList.add(path);
                try {
                    resContentList.add(IOUtils.getStringAsUTF8(resource));
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            });
        });
        assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
        assertEquals(0, pathList.size());
        assertEquals(0, resContentList.size());
    }

    @Test
    default void testGetBaseUrl() throws IOException {
        try (URLSession session = createURLSession()) {
            // verify the result is java.net.URL compatible.
            String urlStr = session.getBaseUrl();
            assertNotNull(urlStr);
            try {
                URL url = URI.create(urlStr).toURL();
            } catch (MalformedURLException ex) {
                fail(ex);
            }
        }
    }

    @Test
    default void testGetUrl() throws IOException {
        try (URLSession session = createURLSession()) {
            String regUrl = session.register("path", new StringResource("contents"));
            String getUrl = session.getUrl("path");
            assertEquals(regUrl, getUrl);

            try {
                URL parseTestURL = URI.create(getUrl).toURL();
            } catch (MalformedURLException e) {
                fail(e);
            }
        }
    }

    @Test
    default void testRegister_String_Generator() throws IOException {
        try (URLSession session = createURLSession()) {
            try {
                String resURL = session.register("path", (OutputStream out) -> {
                    out.write("contents".getBytes());
                });
                URL url = URI.create(resURL).toURL();
                String urlPath = url.getPath();
                assertTrue(urlPath.endsWith("path"));

                // read contents from registration url
                String contents = new String(readAllBytesAndClose(url.openStream()));
                assertEquals("contents", contents);
            } catch (MalformedURLException e) {
                fail(e);
            }
        }
    }

    @Test
    default void testRegister_String_Generator_Overwrite() throws IOException {
        Generator generator = (OutputStream out) -> {
            out.write("contents".getBytes());
        };

        try (URLSession session = createURLSession()) {
            try {
                // register and check twice.
                for (int i = 0; i < 2; i++) {
                    String resURL = session.register("path", generator);
                    URL url = URI.create(resURL).toURL();
                    // read contents from registration url
                    String contents = new String(readAllBytesAndClose(url.openStream()));
                    assertEquals("contents", contents);
                }
            } catch (MalformedURLException e) {
                fail(e);
            }
        }
    }

    @Test
    default void testRegister_String_Generator_Closed() throws IOException {
        URLSession session = createURLSession();
        session.close();
        IOException ioe = assertThrows(IOException.class, () -> {
            session.register("path", (OutputStream out) -> {
                out.write("contents".getBytes());
            });
        });
        assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
    }

    @Test
    default void testRegister_String_Resource() throws IOException {
        try (URLSession session = createURLSession()) {
            try {
                String resURL = session.register("path", new StringResource("contents"));
                URL url = URI.create(resURL).toURL();
                String urlPath = url.getPath();
                assertTrue(urlPath.endsWith("path"));

                // read contents from registration url
                String contents = new String(readAllBytesAndClose(url.openStream()));
                assertEquals("contents", contents);
            } catch (MalformedURLException e) {
                fail(e);
            }
        }
    }

    @Test
    default void testRegister_String_Resource_Overwrite() throws IOException {
        try (URLSession session = createURLSession()) {
            try {
                // repeate twice
                for (int i = 0; i < 2; i++) {
                    String resURL = session.register("path", new StringResource("contents"));
                    URL url = URI.create(resURL).toURL();
                    String urlPath = url.getPath();
                    assertTrue(urlPath.endsWith("path"));
                    // read contents from registration url
                    String contents = new String(readAllBytesAndClose(url.openStream()));
                    assertEquals("contents", contents);
                }
            } catch (MalformedURLException e) {
                fail(e);
            }
        }
    }

    @Test
    default void testRegister_String_Resource_Closed() throws IOException {
        URLSession session = createURLSession();
        session.close();
        IOException ioe = assertThrows(IOException.class, () -> {
            session.register("path", new StringResource("contents"));
        });
        assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
    }

    @Test
    default void testRegister_String_File() throws IOException {
        File tmpFile = File.createTempFile("unitTest", "file-resource");
        try (URLSession session = createURLSession()) {
            writeString(tmpFile.toPath(), "contents");
            try {
                String resURL = session.register("path", tmpFile);
                URL url = URI.create(resURL).toURL();
                String urlPath = url.getPath();
                assertTrue(urlPath.endsWith("path"));

                // read contents from registration url
                String contents = new String(readAllBytesAndClose(url.openStream()));
                assertEquals("contents", contents);
            } catch (MalformedURLException e) {
                fail(e);
            }
        } finally {
            tmpFile.delete();
        }
    }

    @Test
    default void testRegister_String_File_Overwrite() throws IOException {
        File tmpFile = File.createTempFile("unitTest", "file-resource");
        try (URLSession session = createURLSession()) {
            writeString(tmpFile.toPath(), "contents");
            try {
                for (int i = 0; i < 2; i++) {
                    String resURL = session.register("path", tmpFile);
                    URL url = URI.create(resURL).toURL();
                    String urlPath = url.getPath();
                    assertTrue(urlPath.endsWith("path"));
                    // read contents from registration url
                    String contents = new String(readAllBytesAndClose(url.openStream()));
                    assertEquals("contents", contents);
                }
            } catch (MalformedURLException e) {
                fail(e);
            }
        } finally {
            tmpFile.delete();
        }
    }

    @Test
    default void testRegister_String_File_Closed() throws IOException {
        File tmpFile = File.createTempFile("unitTest", "file-resource");
        try {
            URLSession session = createURLSession();
            session.close();
            writeString(tmpFile.toPath(), "contents");
            IOException ioe = assertThrows(IOException.class, () -> {
                session.register("path", tmpFile);
            });
            assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
        } finally {
            tmpFile.delete();
        }
    }

    @Test
    default void testGetResource() throws Exception {
        try (URLSession session = createURLSession()) {
            session.register("path", new StringResource("contents"));
            Resource res = session.getResource("path");
            assertNotNull(res);
            assertEquals("contents", IOUtils.getStringAsUTF8(res));
        }
    }

    @Test
    default void testGetResource_SessionClosed() throws Exception {
        Resource res = new StringResource("Test resource");

        URLSession sess = createURLSession();
        sess.register("test", res);
        sess.close();

        IOException ioe = assertThrows(IOException.class, () -> {
            sess.getResource("test");
        });
        assertEquals(URLSession.SESSION_CLOSE_MSG, ioe.getMessage());
    }

    @Test
    default void testGetResource_ResourceNotFound() throws Exception {
        try (URLSession sess = createURLSession()) {
            // attempt to get inputstream from session with nothing registered.
            assertNull(sess.getResource("test"));
        }
    }

    @Test
    default void testGetRegisteredPaths() throws IOException {
        try (URLSession sess = createURLSession()) {
            Set<String> pathSet = sess.getRegisteredPaths();
            assertNotNull(pathSet);
            assertTrue(pathSet.isEmpty());

            sess.register("path1", new StringResource("This is path1"));
            sess.register("path2", new StringResource("This is path2"));
            sess.register("path3", new StringResource("This is path3"));

            pathSet = sess.getRegisteredPaths();
            assertNotNull(pathSet);
            assertEquals(3, pathSet.size());

            // order of set is not defined
            List<String> pathList = new ArrayList<>(pathSet);
            Collections.sort(pathList);
            assertEquals("path1", pathList.get(0), "list contains path1");
            assertEquals("path2", pathList.get(1), "list contains path2");
            assertEquals("path3", pathList.get(2), "list contains path3");
        }
    }

    @Test
    default void testIsClosed() throws IOException {
        URLSession session = createURLSession();
        assertFalse(session.isClosed());
        session.close();
        assertTrue(session.isClosed());
    }

    @Test
    default void testClose_twice() throws IOException {
        URLSession session = createURLSession();
        assertFalse(session.isClosed());
        session.close();
        assertTrue(session.isClosed());
        session.close();
        assertTrue(session.isClosed());
    }
}
