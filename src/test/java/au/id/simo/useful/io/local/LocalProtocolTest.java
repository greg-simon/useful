package au.id.simo.useful.io.local;

import java.io.IOException;

import au.id.simo.useful.Cleaner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author gsimon
 */
public class LocalProtocolTest {
    
    @Test
    public void testCreateSessionLimits() throws IOException {
        try (Cleaner cleaner = new Cleaner()) {
            for (int i = LocalProtocol.MIN_SESSION_ID; i < LocalProtocol.MAX_SESSION_ID; i++) {
                LocalSession newSession = LocalProtocol.newSession();
                if(i==50) {
                    newSession.close();
                }
                cleaner.closeLater(newSession);
            }
            // create a new one to reach the limit for the 50th session closed above.
            // expect it to succeed.
            cleaner.closeLater(LocalProtocol.newSession());
            
            SessionLimitReachedException ex = assertThrows(SessionLimitReachedException.class, () -> {
                cleaner.closeLater(LocalProtocol.newSession());
            });
            assertTrue(ex.getMessage().startsWith("Session limit reached: "));
        }
    }  
}
