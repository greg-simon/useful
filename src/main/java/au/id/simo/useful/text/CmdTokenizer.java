package au.id.simo.useful.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Breaks a command string into tokens based on white space, while respecting
 * quotes.
 */
public class CmdTokenizer implements Iterable<String> {

    public static List<String> toList(String command) {
        CmdTokenizer tokenizer = new CmdTokenizer(command);
        return tokenizer.toList();
    }

    public static Iterator<String> iterator(String command) {
        CmdTokenizer tokenizer = new CmdTokenizer(command);
        return tokenizer.iterator();
    }

    private final String command;

    public CmdTokenizer(String command) {
        this.command = command;
    }

    public List<String> toList() {
        List<String> tokList = new ArrayList<>();
        for (String tok : this) {
            tokList.add(tok);
        }
        return tokList;
    }

    @Override
    public Iterator<String> iterator() {
        return new CliIterator();
    }

    private class CliIterator implements Iterator<String> {

        private State state = State.NORMAL;
        private int cmdIndex = 0;

        private boolean hasNextCalled;
        private boolean hasNextResult;
        private String nextCommand;

        @Override
        public boolean hasNext() {
            if (hasNextCalled) {
                // now this method can be called repeatedly.
                return hasNextResult;
            }
            StringBuilder sb = new StringBuilder();
            mainwhile:
            while (command != null && cmdIndex < command.length()) {
                char c = command.charAt(cmdIndex++);
                switch (state) {
                    case DOUBLE_QUOTE:
                        if (c == '\"') {
                            state = State.NORMAL;
                        } else {
                            sb.append(c);
                        }
                        break;
                    case SINGLE_QUOTE:
                        if (c == '\'') {
                            state = State.NORMAL;
                        } else {
                            sb.append(c);
                        }
                        break;
                    case BETWEEN_TOKENS:
                        if (!Character.isWhitespace(c)) {
                            state = State.NORMAL;
                            // reset index and loop on this char
                            // again with NORMAL state
                            cmdIndex--;
                        }
                        break;
                    case NORMAL:
                        switch (c) {
                            case '\'':
                                state = State.SINGLE_QUOTE;
                                break;
                            case '"':
                                state = State.DOUBLE_QUOTE;
                                break;
                            default:
                                if (Character.isWhitespace(c)) {
                                    // token complete!
                                    state = State.BETWEEN_TOKENS;
                                    break mainwhile;
                                } else {
                                    sb.append(c);
                                }
                        }
                }
            }
            if (sb.length() > 0) {
                nextCommand = sb.toString();
                hasNextResult = true;
            } else {
                nextCommand = null;
                hasNextResult = false;
            }
            hasNextCalled = true;
            return hasNextResult;
        }

        @Override
        public String next() {
            if (!hasNextCalled) {
                hasNext();
            }
            hasNextCalled = false;

            if (hasNextResult) {
                return nextCommand;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    private enum State {
        NORMAL, DOUBLE_QUOTE, SINGLE_QUOTE, BETWEEN_TOKENS
    }
}
