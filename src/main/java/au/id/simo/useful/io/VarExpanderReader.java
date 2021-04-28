package au.id.simo.useful.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import au.id.simo.useful.CharRingBuffer;

/**
 *
 */
public class VarExpanderReader extends Reader {

    private final Map<String, String> varMap;
    private final Reader reader;
    private final CharRingBuffer buffer;
    private final StringBuilder varNameBuf;
    private final String prefix;
    private final String suffix;

    private State state;

    /**
     * While a value is being written to the buffer it is stored here until
     * complete.
     */
    private String varValue;
    private int varValueIndex;

    public VarExpanderReader(Reader in, Map<String, String> variableMap, String varPrefix, String varSuffix) {
        varMap = variableMap;
        prefix = varPrefix;
        suffix = varSuffix;
        state = State.NORMAL;

        // create input buffer at the maximum length of the largest var key plus
        // variable prefix/suffix markers.
        // create output buffer at the maximum length of the largest var value
        int maxVarKeyLength = 0;
        int maxVarValueLength = 0;
        for (Entry<String, String> entry : variableMap.entrySet()) {
            maxVarKeyLength = Math.max(maxVarKeyLength, entry.getKey().length());
            maxVarValueLength = Math.max(maxVarValueLength, entry.getValue().length());
        }

        // make sure mark is supported
        if (in.markSupported()) {
            reader = in;
        } else {
            reader = new BufferedReader(in, maxVarKeyLength + prefix.length() + suffix.length());
        }

        varNameBuf = new StringBuilder(maxVarKeyLength);
        buffer = new CharRingBuffer(maxVarValueLength + 1);
    }

    /**
     * Fills the ring buffer with as many characters as will fit without
     * overwriting any.
     *
     * @return true if underlying reader is at end of stream
     * @throws IOException if there was an exception thrown while reading from
     * the underlying reader.
     */
    private boolean fillBuffer() throws IOException {
        while (buffer.isNotFull()) {
            if (varValue != null && varValueIndex <= varValue.length()) {
                // there is a value to write. Do this first.
                buffer.put(varValue.charAt(varValueIndex++));
                // reset varValue ready to resume reading from reader next loop.
                if (varValueIndex == varValue.length()) {
                    varValue = null;
                    varValueIndex = 0;
                }
                continue;
            }
            // no var value to write, so read from reader instead.
            int cInt = reader.read();
            if (cInt == -1) {
                return true;
            }
            char c = (char) cInt;
            switch (state) {
                // This state means the prefix has already been read and skipped.
                case VARNAME:
                    // check if variable suffix is next and skip it or not.
                    if (suffix != null && isNextSeq(c, suffix, reader)) {
                        // suffix marks the end of the var name
                        // skip the suffix
                        reader.skip(suffix.length() - 1);
                    } else if (isAcceptableVarNameChar(c)) {
                        // add an acceptable char to end the var name
                        varNameBuf.append(c);

                        if (suffix == null) {
                            // check for end of stream for ext char.
                            reader.mark(1);
                            if (reader.read() != -1) {
                                reader.reset();
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    // end the variable name reading and lookup the value
                    String varName = varNameBuf.toString();
                    // clean varname buffer
                    varNameBuf.delete(0, varNameBuf.length() - 1);
                    varValue = varMap.get(varName);
                    break;
                case NORMAL:
                    if (isNextSeq(c, prefix, reader)) {
                        reader.skip(prefix.length() - 1);
                        state = State.VARNAME;
                        break;
                    }
                    buffer.put(c);
            }
        }
        return false;
    }

    protected boolean isAcceptableVarNameChar(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }

    protected boolean isNextSeq(char currentChar, CharSequence expectedSeq, Reader reader) throws IOException {
        reader.mark(expectedSeq.length());
        if (currentChar != expectedSeq.charAt(0)) {
            return false;
        }
        try {
            for (int i = 1; i < expectedSeq.length(); i++) {
                int nextCharInt = reader.read();
                if (nextCharInt == -1) {
                    return false;
                }
                char nextChar = (char) nextCharInt;
                if (expectedSeq.charAt(i) != nextChar) {
                    return false;
                }
            }
            return true;
        } finally {
            reader.reset();
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int totalRead = 0;
        boolean eos = false;
        while (totalRead < len && !eos) {
            eos = fillBuffer();
            int readCount = buffer.read(cbuf, totalRead, len - totalRead);
            totalRead += readCount;
        }
        // check if end of stream
        if (eos && totalRead == 0) {
            return -1;
        }
        return totalRead;
    }

    @Override
    public int read() throws IOException {
        if (buffer.isNotEmpty()) {
            return buffer.read();
        }
        boolean eos = fillBuffer();
        if (eos && buffer.isEmpty()) {
            return -1;
        }
        return buffer.read();
    }

    @Override
    public boolean ready() throws IOException {
        return buffer.isNotEmpty();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private enum State {
        NORMAL, VARNAME
    }
}
