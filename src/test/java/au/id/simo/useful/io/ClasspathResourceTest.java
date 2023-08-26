package au.id.simo.useful.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class ClasspathResourceTest implements ResourceTest {
    
    private URLSession session;
    
    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        session = new FileSession(tempDir);
    }
    @AfterEach
    public void tearDown() throws IOException {
        session.close();
    }
    
    @Override
    public Resource createResource(byte[] testData, Charset charset) throws IOException {
        String resourcePath = "testdata";
        session.register(resourcePath, new ByteArrayResource(testData));
        ClassLoader loader = new URLClassLoader(new URL[]{
            URI.create(session.getBaseUrl()).toURL()
        });
        return new ClasspathResource(loader, resourcePath);
    }
    
    @Test
    public void testConstructor_String() throws IOException {
        ClasspathResource res = new ClasspathResource("classpath-test.txt");
        assertEquals("Hello World", IOUtils.getStringAsUTF8(res));
    }
    
    @Test
    public void testResourceNotFound() {
        // create a classloader with nothing in it.
        URLClassLoader loader = new URLClassLoader(new URL[0]);
        ClasspathResource res = new ClasspathResource(loader, "nothing-will-be-found");
        IOException ex = assertThrows(IOException.class, res::inputStream);
        assertEquals("Resource not found on Classpath: nothing-will-be-found", ex.getMessage());
    }
}
