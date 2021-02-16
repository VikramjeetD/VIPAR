import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tokens {
    public static final Map<String, String> tokens = new HashMap<>();
    public static final Set<Character> specialChars = new HashSet<>();
    public static final Set<String> reservedWords = new HashSet<>();
    static {
        reservedWords.add("func");
        reservedWords.add("case");
        reservedWords.add("switch");
        reservedWords.add("while");
        reservedWords.add("step");
        reservedWords.add("in");
        reservedWords.add("for");
        reservedWords.add("else");
        reservedWords.add("if");
        reservedWords.add("real");
        reservedWords.add("int");
        reservedWords.add("string");
        reservedWords.add("var");
        reservedWords.add("return");

        tokens.put("return", "TK_RET");
        tokens.put("var", "TK_VARDEC");
        tokens.put("int", "TK_ITYPE");
        tokens.put("real", "TK_RTYPE");
        tokens.put("string", "TK_STYPE");
        tokens.put("if", "TK_IF");
        tokens.put("else", "TK_ELSE");
        tokens.put("for", "TK_FOR");
        tokens.put("in", "TK_IN");
        tokens.put("step", "TK_STEP");
        tokens.put("while", "TK_WHILE");
        tokens.put("switch", "TK_SWITCH");
        tokens.put("case", "TK_CASE");
        tokens.put("func", "TK_FUNC");
        tokens.put("+", "TK_ADD");
        tokens.put("-", "TK_SUB");
        tokens.put("*", "TK_MUL");
        tokens.put("/", "TK_DIV");
        tokens.put("%", "TK_MOD");
        tokens.put("!", "TK_NOT");
        tokens.put("&", "TK_AND");
        tokens.put("|", "TK_OR");
        tokens.put("=", "TK_EQ");
        tokens.put("(", "TK_PARO");
        tokens.put(")", "TK_PARC");
        tokens.put("{", "TK_BRO");
        tokens.put("}", "TK_BRC");
        tokens.put("[", "TK_SQO");
        tokens.put("]", "TK_SQC");
        tokens.put(":", "TK_COL");
        tokens.put(";", "TK_SCOL");
        tokens.put("'", "TK_SQO");
        tokens.put("\"", "TK_DQO");
        tokens.put("?", "TK_QMA");
        tokens.put( ".", "TK_DOT");
        tokens.put(",", "TK_COM");
        tokens.put( ">", "TK_GT");
        tokens.put( "<", "TK_LT");

        tokens.put(":=", "TK_ASSIGN");
        tokens.put("==", "TK_EQLB");
        tokens.put("<=", "TK_LTE");
        tokens.put(">=", "TK_GTE");
        tokens.put("!=", "TK_NEQ");
        tokens.put("&&", "TK_LAND");
        tokens.put("||", "TK_LOR");
        tokens.put("...", "TK_RIN");
        tokens.put("..<", "TK_REX");
        tokens.put("->", "TK_RET");

        specialChars.add('$');
        specialChars.add('_');
    }

}
