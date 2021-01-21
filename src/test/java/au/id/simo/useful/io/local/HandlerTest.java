package au.id.simo.useful.io.local;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class HandlerTest {
    protected static final String SYS_PROP_KEY = "java.protocol.handler.pkgs";
    private static String initialPropertyValue;

    @BeforeAll
    public static void beforeAll() {
        initialPropertyValue = System.getProperty(SYS_PROP_KEY);
    }

    @BeforeEach
    public void beforeEach() {
        if (initialPropertyValue == null) {
            System.clearProperty(SYS_PROP_KEY);
            return;
        }
        System.setProperty(SYS_PROP_KEY, initialPropertyValue);
    }

    @Test
    public void testRegisterHandlerIfRequired() {
        Handler.registerHandlerIfRequired();
        String postAddedSysProp = System.getProperty(SYS_PROP_KEY, "");
        assertTrue(postAddedSysProp.contains(Handler.URL_HANDLER_PACKAGE));
    }

    @Test
    public void testRegisterHandlerIfRequired_Multiple() {
        String postAddedSysProp;
        for (int i = 0; i < 2; i++) {
            Handler.registerHandlerIfRequired();
            postAddedSysProp = System.getProperty(SYS_PROP_KEY, "");
            assertTrue(postAddedSysProp.contains(Handler.URL_HANDLER_PACKAGE));
        }
    }
    
    @Test
    public void testRegisterHandlerIfRequired_NullProperty() {
        System.clearProperty(SYS_PROP_KEY);
        Handler.registerHandlerIfRequired();
        String postAddedSysProp = System.getProperty(SYS_PROP_KEY, "");
        assertEquals(Handler.URL_HANDLER_PACKAGE, postAddedSysProp);
    }
    
    @Test
    public void testRegisterHandlerIfRequired_AlreadySetProperty() {
        System.setProperty(SYS_PROP_KEY, "some.other.package");
        Handler.registerHandlerIfRequired();
        String postAddedSysProp = System.getProperty(SYS_PROP_KEY, "");
        
        String expectedStr = Handler.URL_HANDLER_PACKAGE + "|some.other.package";
        assertEquals(expectedStr, postAddedSysProp);
    }
}
