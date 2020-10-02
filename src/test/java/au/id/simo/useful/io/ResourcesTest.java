package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ResourcesTest {

    @Test
    public void testResolveResourceName() {
        Resources res;
        res = new ResourcesImpl("path/%s");
        assertEquals("path/resource", res.resolveResourceName("resource"));

        res = new ResourcesImpl("path/");
        assertEquals("path/resource", res.resolveResourceName("resource"));

        res = new ResourcesImpl("path/%s.txt");
        assertEquals("path/resource.txt", res.resolveResourceName("resource"));

        res = new ResourcesImpl("path/to/%s.template");
        assertEquals("path/to/resource.template", res.resolveResourceName("resource"));

        res = new ResourcesImpl("path/%d.template");
        assertEquals("path/10.template", res.resolveResourceName(10));
        assertEquals("path/20.template", res.resolveResourceName(20));

        res = new ResourcesImpl("path/%02d.template");
        assertEquals("path/01.template", res.resolveResourceName(1));
        assertEquals("path/45.template", res.resolveResourceName(45));

        res = new ResourcesImpl("%s_%03d.txt");
        assertEquals("string_001.txt", res.resolveResourceName("string", 1));
        assertEquals("foobar_12345.txt", res.resolveResourceName("foobar", 12345));

        res = new ResourcesImpl(null);
        assertEquals("testing String.valueOf",
                res.resolveResourceName( new StringBuilder("testing String.valueOf")));
        assertEquals(
                "testing String.valueOf",
                res.resolveResourceName(
                        new StringBuilder("testing String.valueOf"),
                        "other",
                        "random",
                        "args",
                        "to",
                        "ignore",
                        100,
                        new Object()),
                "other args should be ignored."
        );
    }

    @Test
    public void testNoExceptionOnGet_OnlyOnResource() throws IOException {
        Resources res = new ResourcesImpl("path/to/");
        Resource resource = res.get("resource");
        assertNotNull(resource);

        IOException ioe = assertThrows(IOException.class, () -> {
            InputStream in = resource.inputStream();
        });
        assertEquals(ioe.getMessage(), "ResourcesImpl: Not implemented");
    }

    public class ResourcesImpl extends Resources {

        public ResourcesImpl(String pattern) {
            super(pattern);
        }

        @Override
        public InputStream createStream(String resolvedResourceName)
                throws IOException {
            throw new IOException("ResourcesImpl: Not implemented");
        }
    }
}
