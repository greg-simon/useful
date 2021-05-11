package au.id.simo.useful.text;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CmdTokenizerTest {

    public CmdTokenizerTest() {
    }

    @Test
    public void testIterator() {
        String cmd = "one \t 'this is two'  three \"this is four\" ";
        Iterator<String> itr = CmdTokenizer.iterator(cmd);
        assertTrue(itr.hasNext());
        assertEquals("one", itr.next());
        assertTrue(itr.hasNext());
        assertEquals("this is two", itr.next());
        assertTrue(itr.hasNext());
        assertEquals("three", itr.next());
        assertTrue(itr.hasNext());
        assertEquals("this is four", itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    public void testList() {
        String cmd = "one \t 'this is two'  three \"this is four\" ";
        List<String> list = CmdTokenizer.toList(cmd);
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals("one", list.get(0));
        assertEquals("this is two", list.get(1));
        assertEquals("three", list.get(2));
        assertEquals("this is four", list.get(3));
    }

    @Test
    public void testNotCallingHasNext() {
        String cmd = "one";
        Iterator<String> itr = new CmdTokenizer(cmd).iterator();
        assertEquals("one", itr.next());

        assertThrows(NoSuchElementException.class, () -> {
            itr.next();
        });
    }

    @Test
    public void testRepeatedlyCallingHasNext() {
        String cmd = "one";
        Iterator<String> itr = new CmdTokenizer(cmd).iterator();
        assertTrue(itr.hasNext());
        assertTrue(itr.hasNext());
        assertTrue(itr.hasNext());
        assertTrue(itr.hasNext());
        assertEquals("one", itr.next());

        assertFalse(itr.hasNext());
    }

    @Test
    public void testTypicalDockerCmd() {
        String cmd = "/usr/bin/docker run --rm --net atlas --ip 10.20.30.05 -p 80:80 -p 443:443 --env-file /usr/local/etc/woodside.env -v \"nginx-cache:/var/cache/nginx\"     -v \"nginx-static-content:/usr/share/nginx/html\"     -v \"/etc/ssl/certs:/etc/ssl/certs:Z\" --name %p PLACEHOLDER_TAG";
        CmdTokenizer tok = new CmdTokenizer(cmd);
        Iterator<String> itr = tok.iterator();

        assertEquals("/usr/bin/docker", itr.next());
        assertEquals("run", itr.next());
        assertEquals("--rm", itr.next());
        assertEquals("--net", itr.next());
        assertEquals("atlas", itr.next());
        assertEquals("--ip", itr.next());
        assertEquals("10.20.30.05", itr.next());
        assertEquals("-p", itr.next());
        assertEquals("80:80", itr.next());
        assertEquals("-p", itr.next());
        assertEquals("443:443", itr.next());
        assertEquals("--env-file", itr.next());
        assertEquals("/usr/local/etc/woodside.env", itr.next());
        assertEquals("-v", itr.next());
        assertEquals("nginx-cache:/var/cache/nginx", itr.next());
        assertEquals("-v", itr.next());
        assertEquals("nginx-static-content:/usr/share/nginx/html", itr.next());
        assertEquals("-v", itr.next());
        assertEquals("/etc/ssl/certs:/etc/ssl/certs:Z", itr.next());
        assertEquals("--name", itr.next());
        assertEquals("%p", itr.next());
        assertEquals("PLACEHOLDER_TAG", itr.next());

        assertFalse(itr.hasNext());
    }

    @Test
    public void testNullCommand() {
        String cmd = null;
        CmdTokenizer tok = new CmdTokenizer(cmd);

        Iterator<String> itr = tok.iterator();
        assertFalse(itr.hasNext());

        List<String> emptyList = tok.toList();
        assertNotNull(emptyList);
        assertEquals(0, emptyList.size());
    }
}
