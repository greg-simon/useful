package au.id.simo.useful.io.local;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LocalProtocolTest {
    private Object lock = new Object();
    @Test
    @ResourceLock(value = "lock")
    public void testCreateAllSessions() throws IOException {
        // creating all sessions is a lengthy operation, so test everything
        // while we're at it.
        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;
        
        for (int i = 0; i < LocalProtocol.maxSessions(); i++) {
            LocalSession ls = LocalProtocol.newSession();
            minId = Math.min(minId, ls.getId());
            maxId = Math.max(maxId, ls.getId());
        }
        assertEquals(LocalProtocol.maxSessions(), LocalProtocol.sessionCount());
        assertEquals(LocalProtocol.MIN_SESSION_ID, minId, "Min");
        assertEquals(LocalProtocol.MAX_SESSION_ID, maxId, "Max");

        SessionLimitReachedException ex = assertThrows(SessionLimitReachedException.class, LocalProtocol::newSession);
        assertTrue(ex.getMessage().startsWith("Session limit reached: "));
        
        // close the last session, and create another to replace it
        LocalSession lastSession = LocalProtocol.getSession(LocalProtocol.MAX_SESSION_ID);
        assertNotNull(lastSession);
        lastSession.close();
        LocalProtocol.newSession();
        
        // verify closeAll works
        assertEquals(LocalProtocol.maxSessions(), LocalProtocol.sessionCount());
        assertEquals(LocalProtocol.maxSessions(), LocalProtocol.closeAllSessions());
        assertEquals(0, LocalProtocol.sessionCount());
    }
}
