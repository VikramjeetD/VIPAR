import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Lexer {
    private final int BUFFER_SIZE = 20;
    // Buffer pair to handle split tokens
    private final char[] buffer0;
    private final char[] buffer1;
    private int begin;
    private int end;
    private int endBufferNumber; // Buffer which end belongs to (0 or 1)
    private int beginBufferNumber; // Buffer which begin belongs to (0 or 1)
    private int state;
    private final Map<String, String> tokens; // Hashmap of lexemes -> tokens
    private final Set<Character> specialChars; // Special charset available for use in variable names
    private final Set<String> reservedWords; // Reserved word set.
    private final Set<Character> safeTokens;
    private final Reader reader;
    private final Parser parser;
    private int line; // Line number
    private int pos; // Position in line
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private StringBuffer stringLiteral; // String literal variable for exceeding beyond buffer capacities

    Lexer(String filePath) throws IOException {
        // Append EOF to file to check for end condition
        FileWriter fw = new FileWriter(filePath, true);
        char ch = '\n';
        fw.write(ch);
        ch = 26;
        fw.write(ch);
        fw.flush();
        fw.close();

        buffer0 = new char[BUFFER_SIZE];
        buffer1 = new char[BUFFER_SIZE];
        begin = 0;
        end = 0;
        endBufferNumber = 1; // Will be set to 0 when we fill buffer for first time
        beginBufferNumber = 0;
        state = 0;
        line = 1;
        pos = 1;
        stringLiteral = new StringBuffer();
        tokens = Tokens.tokens;
        specialChars = Tokens.specialChars;
        reservedWords = Tokens.reservedWords;
        safeTokens = Tokens.safeTokens;
        reader = new Reader(filePath);
        parser = new Parser();
        fillBuffer();
        getTokens();
    }

    /**
     * Called when end exceeds buffer
     * Reads into other buffer from file and sets
     * end to point to beginning of the other buffer
     */
    public void fillBuffer() throws IOException {
        endBufferNumber = 1 - endBufferNumber;
        if (endBufferNumber == 0) {
            reader.readIntoBuffer(buffer0);
        } else {
            reader.readIntoBuffer(buffer1);
        }
        end = 0;
    }

    /**
     * While not EOF at end, perform transition
     * If end exceeds buffer after transition, refill buffers
     */
    public void getTokens() throws IOException {
        while (!((endBufferNumber == 0 && buffer0[end] == 26) || (endBufferNumber == 1 && buffer1[end] == 26))) {
            char nextChar = endBufferNumber == 0 ? buffer0[end] : buffer1[end];
            Pair pair = transition(nextChar);
            if (pair != null) parser.addLexeme(pair, line, pos);
            if (end >= BUFFER_SIZE) {
                fillBuffer();
            }
        }
        if (state == 18) {
            System.out.println(ANSI_RED + "LEXER ERROR: End of string literal not found on line " + line + ANSI_RESET);
        } else {
            parser.addLexeme(new Pair("$", "TK_END"), line, pos);
        }
    }

    public Pair transition(char ch) {
        Pair pair = null;
        switch (state) {
            case 0:
                switch (ch) {
                    case ' ':
                        incBeginEnd(); break;
                    case '\t': pos += 3; incBeginEnd(); break;
                    case '\n': pos = -1; incBeginEnd(); line++; break;
                    case '?':
                    case '+': case '*':
                    case '(': case ')': case '{': case '}': case '[': case ']':
                    case ',': case ';':
                        String lexeme = Character.toString(ch);
                        String token = tokens.get(Character.toString(ch));
//                        System.out.println("Lexeme: " + lexeme + "; Token: " + token + "; Line: " + line);
                        incBeginEnd();
                        pair = new Pair(lexeme, token);
                        break;
                    case ':':
                        state = 4;
                        incEnd();
                        break;
                    case '=':
                        state = 5;
                        incEnd();
                        break;
                    case '<':
                        state = 6;
                        incEnd();
                        break;
                    case '>':
                        state = 7;
                        incEnd();
                        break;
                    case '!':
                        state = 8;
                        incEnd();
                        break;
                    case '&':
                        state = 9;
                        incEnd();
                        break;
                    case '|':
                        state = 10;
                        incEnd();
                        break;
                    case '.':
                        state = 12;
                        incEnd();
                        break;
                    case '/':
                        state = 13;
                        incEnd();
                        break;
                    case '-':
                        state = 17;
                        incEnd();
                        break;
                    case '\'':
                        state = 18;
                        stringLiteral = new StringBuffer("'");
                        incEnd();
                        break;
                    default:
                        if (Character.isLetter(ch) || specialChars.contains(ch)) {
                            state = 1;
                            incEnd();
                        } else if (Character.isDigit(ch)) {
                            state = 2;
                            incEnd();
                        } else {
                            state = 19;
                            System.out.println(ANSI_RED + "LEXER ERROR: {Illegal character: " + ch + " at line " + line + " at position " + pos + "}" + ANSI_RESET);
                        }
                }
                break;
            case 1:
                if (specialChars.contains(ch) || Character.isLetterOrDigit(ch)) {
                    incEnd();
                } else {
                    String lexeme = getLexeme(0);
                    if (reservedWords.contains(lexeme)) {
                        pair = printAndReset(tokens.get(lexeme), 0);
                    } else {
                        pair = printAndReset("TK_IDF", 0);
                    }
                }
                break;
            case 2:
                if (Character.isDigit(ch)) {
                    incEnd();
                } else if (ch == '.') {
                    state = 3;
                    incEnd();
                } else {
                    pair = printAndReset("TK_INT", 0);
                }
                break;
            case 3:
                if (Character.isDigit(ch)) {
                    state = 20;
                    incEnd();
                } else {
                    retract(1);
                    pair = printAndReset("TK_INT", 0);
                }
                break;
            case 4:
                if (ch == '=') {
                    pair = printAndReset(tokens.get(":="), 1);
                } else {
                    pair = printAndReset(tokens.get(":"), 0);
                }
                break;
            case 5:
                if (ch == '=') {
                    pair = printAndReset(tokens.get("=="), 1);
                } else {
                    pair = printAndReset(tokens.get("="), 0);
                }
                break;
            case 6:
                if (ch == '=') {
                    pair = printAndReset(tokens.get("<="), 1);
                } else {
                    pair = printAndReset(tokens.get("<"), 0);
                }
                break;
            case 7:
                if (ch == '=') {
                    pair = printAndReset(tokens.get(">="), 1);
                } else {
                    pair = printAndReset(tokens.get(">"), 0);
                }
                break;
            case 8:
                if (ch == '=') {
                    pair = printAndReset(tokens.get("!="), 1);
                } else {
                    pair = printAndReset(tokens.get("!"), 0);
                }
                break;
            case 9:
                if (ch == '&') {
                    pair = printAndReset(tokens.get("&&"), 1);
                } else {
                    pair = printAndReset(tokens.get("&"), 0);
                }
                break;
            case 10:
                if (ch == '|') {
                    pair = printAndReset(tokens.get("||"), 1);
                } else {
                    pair = printAndReset(tokens.get("|"), 0);
                }
                break;
            case 11:
                if (ch == '.') {
                    pair = printAndReset(tokens.get("..."), 1);
                } else if (ch == '<') {
                    pair = printAndReset(tokens.get("..<"), 1);
                } else {
                    state = 19;
                    System.out.println(ANSI_RED + "LEXER ERROR: Unexpected token at line " + line + " at position " + (pos - 1) + ANSI_RESET);
                    incEnd();
                }
                break;
            case 12:
                if (ch == '.') {
                    state = 11;
                    incEnd();
                } else {
                    pair = printAndReset(tokens.get("."), 0);
                }
                break;
            case 13:
                if (ch == '/') {
                    state = 14;
                    incEnd();
                } else if (ch == '*') {
                    state = 15;
                    incEnd();
                } else {
                    pair = printAndReset(tokens.get("/"), 0);
                }
                break;
            // Single line comment, stay in state 14 until we see newline, then reset
            case 14:
                if (ch == '\n') {
                    line++;
                    pos = 0;
                    incEnd();
                    reset();
                } else {
                    incEnd();
                }
                break;
            // Multi line comment, go to state 16 when we see *
            case 15:
                if (ch == '*') {
                    state = 16;
                } else if (ch == '\n') {
                    pos = 0;
                    line++;
                }
                incEnd();
                break;
            // Multi line comment ends when we see /, if anything else, it continues
            case 16:
                if (ch == '/') {
                    incEnd();
                    reset();
                } else {
                    if (ch == '\n') {
                        line++;
                        pos = 0;
                    }
                    state = 15;
                    incEnd();
                }
                break;
            // If >, then we get -> which is token for return types
            case 17:
                if (ch == '>') {
                    pair = printAndReset(tokens.get("->"), 1);
                } else {
                    pair = printAndReset(tokens.get("-"), 0);
                }
                break;
            case 18:
                if (ch == '\'') {
                    stringLiteral.append("'");
                    String lexeme = stringLiteral.toString();
//                    System.out.println("Lexeme: " + lexeme + "; Token: TK_STR" + "; Line: " + line);
                    pair = new Pair(lexeme, "TK_STR");
                    end += 1;
                    pos += 1;
                    reset();
                } else if (ch != '\n' && ch != '\r' && ch != '\t'){
                    stringLiteral.append(ch);
                    incEnd();
                } else {
                    if (ch == '\n') {
                        line++;
                        pos = 0;
                    }
                    incEnd();
                }
                break;
            case 19:
                if (safeTokens.contains(ch)) {
                    reset();
                } else {
                    incEnd();
                }
                break;
            case 20:
                if (Character.isDigit(ch)) {
                    incEnd();
                } else {
                    pair = printAndReset("TK_REAL", 0);
                }
                break;
        }
        return pair;
    }

    /**
     * If begin and end are in the same buffer, simply take whats between them
     * Otherwise, concatenate what comes after begin and what comes before end from the corresponding buffers
     */
    private String getLexeme(int forward) {
        if (beginBufferNumber == endBufferNumber) {
            if (beginBufferNumber == 0) {
                return new String(buffer0, begin, end - begin + forward);
            } else {
                return new String(buffer1, begin, end - begin + forward);
            }
        } else if (beginBufferNumber == 0 && endBufferNumber == 1) {
            return (new String(buffer0, begin, buffer0.length - begin) + new String(buffer1, 0, end + forward));
        } else {
            return (new String(buffer1, begin, buffer0.length - begin) + new String(buffer0, 0, end + forward));
        }
    }

    /**
     *
     * @param forward forward = 1 if current character is part of current token
     *                forward = 0 otherwise (Eg: separators: .,,,;,:, etc.
     */
    private Pair printAndReset(String tokenID, int forward) {
        String lexeme = getLexeme(forward);
//        System.out.println("Lexeme: " + lexeme + "; Token: " + tokenID + "; Line: " + line);
        end += forward;
        pos += forward;
        reset();
        return new Pair(lexeme, tokenID);
    }

    /**
     * Move end back steps times.
     * If end goes beyond left of buffer, switch end buffers accordingly
     */
    private void retract(int steps) {
        for (int i = 0; i < steps; i++) {
            end--;
            pos--;
            if (end == -1) {
                end = BUFFER_SIZE - 1;
                endBufferNumber = 1 - endBufferNumber;
            }
        }
    }

    private void incEnd() {
        pos++;
        end++;
    }
    private void incBeginEnd() {
        incEnd();
        begin = end;
        beginBufferNumber = endBufferNumber;
    }
    private void reset() {
        state = 0;
        begin = end;
        beginBufferNumber = endBufferNumber;
    }
}


class Pair {
    String lexeme, token;
    Pair(String lexeme, String token) {
        this.lexeme = lexeme;
        this.token = token;
    }
}
