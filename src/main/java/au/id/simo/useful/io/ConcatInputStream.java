package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Un-optimised simplistic implementation
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
    
    @Override
    public int read() throws IOException {
        InputStream current = currentStream();
        if(currentStream == null) {
            return -1;
        }
        int returnValue = current.read();
        if (returnValue == -1) {
            currentStream = null;
        }
        return returnValue;
    }

    @Override
    public void close() throws IOException {
        for(InputStream in: inputStreams) {
            in.close();
        }
    }
}
