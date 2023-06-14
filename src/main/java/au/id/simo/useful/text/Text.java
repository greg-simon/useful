package au.id.simo.useful.text;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import au.id.simo.useful.text.TextTable.Row;

/**
 * Utility class most useful for printing text to the console.
 */
public class Text {

    private Text() {
    }

    public static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder(times);
        repeat(sb, c, times);
        return sb.toString();
    }

    public static void repeat(StringBuilder sb, char c, int times) {
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
    }

    public static void repeat(StringBuilder sb, int insertPosition, char c, int times) {
        sb.insert(insertPosition, new RepeatCharSequence(c, times));
    }

    public static void print(ResultSet rs) throws SQLException {
        print(rs, System.out);
    }

    public static void print(ResultSet rs, PrintStream out) throws SQLException {
        ResultSetMetaData rsmeta = rs.getMetaData();
        int columnCount = rsmeta.getColumnCount();
        String[] headings = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            headings[i] = rsmeta.getColumnName(i + 1);
        }
        // headings
        TextTable ct = new TextTable();
        ct.setHeadings((Object[]) headings);
        while (rs.next()) {
            Row row = ct.newRow();
            for (int i = 0; i < columnCount; i++) {
                row.addField(rs.getString(i + 1));
            }
        }
        ct.print(out);
    }
}
