package au.id.simo.useful.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class EnvVarExpanderTest implements VarExpanderTest {

    @Test
    public void testPut() {
    }

    @Test
    public void testExpand() {
    }

    @Override
    public VarExpander getInstance() {
        return new EnvVarExpander();
    }
}
