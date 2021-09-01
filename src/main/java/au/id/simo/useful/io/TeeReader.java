package au.id.simo.useful.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 *
 */
public class TeeReader extends FilterReader {

    private final Writer writer;

    public TeeReader(Reader reader, Writer writer) {
        super(reader);
        this.writer = writer;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int readCount = super.read(cbuf, off, len);
        if (readCount == -1) {
            return -1;
        }
        writer.write(cbuf, off, readCount);
        return readCount;
    }

    @Override
    public int read() throws IOException {
        int ch = super.read();
        if (ch == -1) {
            return -1;
        }
        writer.write(ch);
        return ch;
    }

    @Override
    public void close() throws IOException {
        writer.close();
        super.close();
    }
}
