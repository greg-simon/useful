package au.id.simo.useful;

import java.util.HashMap;
import java.util.Map;

/**
 * Expands variables in a format like {@code %var}.
 * 
 * To return an actual '%' char, two '%' chars must be used. e.g. '%%'
 */
public class PercentVarExpander implements VarExpander {
    private final Map<String, String> varMap;
    
    public PercentVarExpander() {
        varMap = new HashMap<>();
    }
    
    @Override
    public VarExpander put(String varName, String varValue) {
        this.varMap.put(varName, varValue);
        return this;
    }

    @Override
    public String expand(String sourceStr) {
        StringBuilder output = new StringBuilder();
        StringBuilder varName = new StringBuilder();
        State state = State.NORMAL;
        
        for(int index=0; index < sourceStr.length(); index++) {
            char c = sourceStr.charAt(index);
            switch (state) {
                case VARNAME:
                    if(isAcceptableVarNameChar(c)) {                        
                        varName.append(c);
                    } else {                        
                        // end the variable name reading and lookup the value
                        output.append(expandVar(varName));
                        // dont forget to add the non varname char to the output
                        output.append(c);
                        state = State.NORMAL;
                    }
                    break;
                case NORMAL:
                    switch (c) {
                        case '%':
                            if (isNextChar('%', sourceStr, index)) {
                                // ecscaped %, add one and skip the next.
                                output.append(c);
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
        if(varName.length()>0) {
            output.append(expandVar(varName));
        }
        return output.toString();
    }
    
    private String expandVar(StringBuilder varName) {
        String value = varMap.get(varName.toString());
        clearSB(varName);
        if(value == null) {
            return "";
        } else {
            return value;
        }
    }
    
    private void clearSB(StringBuilder sb) {
        sb.delete(0, sb.length());
    }
    
    private enum State {
        NORMAL
        ,VARNAME
    }
}
