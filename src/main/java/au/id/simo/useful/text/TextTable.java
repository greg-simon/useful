package au.id.simo.useful.text;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import au.id.simo.useful.collections.ConcatIterator;

/**
 * For use when writing a table to the console, it ensures all the columns line
 * up correctly.
 * <p>
 * Add fields one row at a time, then iterate over each formatted row to print
 * them.
 */
public class TextTable implements Iterable<CharSequence> {
    
    private static final int TAB_SIZE = 8;
    
    private final List<String> headings;
    private final List<List<String>> rowList;
    private final List<Integer> maxColSizeList;

    public TextTable() {
        this.headings = new ArrayList<>();
        this.rowList = new ArrayList<>();
        this.maxColSizeList = new ArrayList<>();
    }
    
    public void setHeadings(Object... colnames) {
        headings.clear();
        for (int i = 0; i < colnames.length; i++) {
            String heading = String.valueOf(colnames[i]);
            setMaxFieldSize(i, heading.length());
            headings.add(heading);
        }
    }
    
    public void addRow(Object... fields) {
        List<String> fieldList = new ArrayList<>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            String fieldStr = String.valueOf(fields[i]);
            setMaxFieldSize(i, fieldStr.length());
            fieldList.add(fieldStr);
        }
        rowList.add(fieldList);
    }
    
    private void setMaxFieldSize(int colNo, int size) {
        // pad size of list if required.
        for (int i = 0; i < colNo - (maxColSizeList.size()-1); i++) {
            maxColSizeList.add(0);
        }
        int existingLength = maxColSizeList.get(colNo);
        maxColSizeList.set(colNo, Math.max(size, existingLength));
    }
    
    
    public interface Row {
        Row addField(Object obj);
    }
    /**
     * Builder api for rows.
     * @return a new row
     */
    public Row newRow() {
        final List<String> fieldList = new ArrayList<>();
        rowList.add(fieldList);
        return new Row() {
            @Override
            public Row addField(Object obj) {
                int fieldId = fieldList.size();
                String fieldStr = String.valueOf(obj);
                fieldList.add(fieldStr);
                setMaxFieldSize(fieldId, fieldStr.length());
                return this;
            }
        };
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<CharSequence> iterator() {
        final Iterator<List<String>> rowsItr;
        if (headings.isEmpty()) {
            rowsItr = rowList.iterator();
        } else {
            // headings exist, so create the formatting around them
            List<String> headingDividers = new ArrayList<>(maxColSizeList.size());
            for (int i=0;i<maxColSizeList.size(); i++) {
                int colWidth = (maxColSizeList.get(i) / TAB_SIZE) + 1 * 8;
                headingDividers.add(Text.repeat('-', colWidth));
            }
            rowsItr = new ConcatIterator<>(
                    Arrays.asList(headings, headingDividers).iterator(),
                    rowList.iterator()
            );
        }
        
        return new Iterator<CharSequence>() {

            @Override
            public boolean hasNext() {
                return rowsItr.hasNext();
            }

            @Override
            public CharSequence next() {
                List<String> fieldList = rowsItr.next();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fieldList.size(); i++) {
                    String field = fieldList.get(i);
                    int maxFieldLength = maxColSizeList.get(i);
                    int maxTabs = maxFieldLength / TAB_SIZE;
                    int fieldTabs = field.length() / TAB_SIZE;
                    // + 1 because we always want at least one tab
                    int tabs = (maxTabs - fieldTabs) + 1;
                    sb.append(field);
                    Text.repeat(sb, '\t', tabs);
                }
                return sb;
            }
        };
    }
    
    public void print(PrintStream writer) {
        for(CharSequence seq: this) {
            writer.println(seq);
        }
    }
    
    public void print(PrintWriter writer) {
        for(CharSequence seq: this) {
            writer.println(seq);
        }
    }
}
