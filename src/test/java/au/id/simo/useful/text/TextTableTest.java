package au.id.simo.useful.text;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class TextTableTest {

    @Test
    public void testSomeMethod() {
        TextTable table = new TextTable();
        table.setHeadings("Row No.", "col2", "col3", "col4", "col5");
        table.addRow("Row 1:","Testing", "one", "two", "three");
        table.print(System.out);
    }
}
