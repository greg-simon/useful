package au.id.simo.useful.text;

import java.util.HashMap;
import java.util.Map;

/**
 * Expands variables found in provided {@code String} formatted like
 * {@code $VAR} or {@code ${VAR}}.
 *
 * And variables with no found value will be simply removed.
 */
public class EnvVarExpander implements VarExpander {

    private final Map<String, String> varMap;

    public EnvVarExpander() {
        varMap = new HashMap<>();
    }

    public EnvVarExpander(Map<String, String> varMap) {
        this.varMap = varMap;
    }

    /**
     * Builder style method for populating variable mappings.
     *
     * @param key variable name
     * @param value variable value
     * @return this EnvVarExpander for further method chaining.
     */
    @Override
    public EnvVarExpander put(String key, String value) {
        varMap.put(key, value);
        return this;
    }

    @Override
    public String expand(String sourceStr) {
        StringBuilder output = new StringBuilder();
        StringBuilder varName = new StringBuilder();
        State state = State.NORMAL;

        for (int index = 0; index < sourceStr.length(); index++) {
            char c = sourceStr.charAt(index);
            switch (state) {
                case BRACE_VARNAME:
                    if (c == '}') {
                        // end the variable name reading
                        // and don't add the closing brace to the output.
                        output.append(expandVar(varName));
                        state = State.NORMAL;
                    } else {
                        varName.append(c);
                    }
                    break;
                case VARNAME:
                    if (isAcceptableVarNameChar(c)) {
                        varName.append(c);
                    } else {
                        // end the variable name reading and lookup the value
                        output.append(expandVar(varName));
                        // dont forget to add the whitespace to the output
                        output.append(c);
                        state = State.NORMAL;
                    }
                    break;
                case NORMAL:
                    switch (c) {
                        case '$':
                            if (isNextChar('$', sourceStr, index)) {
                                // ecscaped $, add one and skip the next.
                                output.append(c);
                                index++;
                            } else if (isNextChar('{', sourceStr, index)) {
                                state = State.BRACE_VARNAME;
                                // skip adding the brace char to the output
                                index++;
                            } else {
                                state = State.VARNAME;
                            }
                            break;
                        default:
                            output.append(c);
                    }
            }
        }
        // for when the variable name is the last part of the source str.
        if (varName.length() > 0) {
            output.append(expandVar(varName));
        }
        return output.toString();
    }

    private String expandVar(StringBuilder varName) {
        String value = varMap.get(varName.toString());
        clearSB(varName);
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private void clearSB(StringBuilder sb) {
        sb.delete(0, sb.length());
    }

    private enum State {
        NORMAL, VARNAME, BRACE_VARNAME
    }
}
