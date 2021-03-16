package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Joins multiple InputStreams into one.
 */
public class ConcatInputStream extends InputStream {
    
    private final List<InputStream> inputStreams;
    private final Iterator<InputStream> readStreamsItr;
    private InputStream currentStream;

    public ConcatInputStream(InputStream... inputStreams) {
        this.inputStreams = Arrays.asList(inputStreams);
        this.readStreamsItr = this.inputStreams.iterator();
    }
    
    private InputStream currentStream() {
        if (currentStream==null) {
            if(readStreamsItr.hasNext()) {
                currentStream = readStreamsItr.next();
            }
        }
        return currentStream;
    }
    
    private void currentEnded() {
        currentStream = null;
    }
    
    @Override
    public int read() throws IOException {
        InputStream current = currentStream();
        while(current != null) {
            int returnValue = current.read();
            if (returnValue == -1) {
                currentEnded();
                current = currentStream();
            } else {
                return returnValue;
            }
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        InputStream current = currentStream();
        while(current != null) {
            int readCount = current.read(b, off, len);
            if (readCount == -1) {
                currentEnded();
                current = currentStream();
            } else {
                return readCount;
            }
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        for(InputStream in: inputStreams) {
            in.close();
        }
    }
}
