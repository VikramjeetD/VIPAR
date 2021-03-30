import java.util.*;

public class Parser {
    private int currentState;
    private final Stack<String> stack;
    private final Map<String, String> tokens;
    private final Map<String, Integer> parsingMap;
    private final List<List<String>> table;
    private final List<List<String>> grammar;
    private final Set<String> literalTokens;

    Parser() {
        parsingMap = ParseTable.parsingMap;
        table = ParseTable.table;
        grammar = ParseTable.grammar;
        tokens = Tokens.tokens;
        literalTokens = new HashSet<>(Arrays.asList("TK_IDF", "TK_INT", "TK_REAL", "TK_STR"));

        stack = new Stack<>();
        stack.push("0");
    }

    void addLexeme(Pair pair) {
        String lexeme = pair.lexeme;
        String token = pair.token;
//        System.out.print("Stack before " + lexeme + ": ");
//        System.out.println(Arrays.toString(stack.toArray()));
        String action, symbolToPush = lexeme;
        if (lexeme.equals("funcname") || tokens.containsKey(lexeme) || token.equals("TK_SAFE")) {
            symbolToPush = lexeme;
        } else if (literalTokens.contains(token)) {
            symbolToPush = "variable";
        } else if (lexeme.equals("->")) {
            symbolToPush = "arrow";
        }

        action = table.get(currentState).get(parsingMap.get(symbolToPush));
        try {
            if (action.length() == 0) {
                System.out.println("SYNTAX ERROR for lexeme: '" + lexeme + "' and token: '" + token + "'!");
            } else {
                switch (action.charAt(0)) {
                    case 'r':
                        while (action.length() > 0 && action.charAt(0) == 'r') {
//                            System.out.println("need to reduce stack for " + lexeme);
                            reduceStack(Integer.parseInt(action.substring(1)));
                            action = table.get(currentState).get(parsingMap.get(symbolToPush));
                        }
                        addLexeme(pair);
                        break;
                    case 's':
                        stack.push(symbolToPush);
                        stack.push(action.substring(1));
                        currentState = Integer.parseInt(stack.peek());
                        break;
                    case 'a':
                        System.out.println("CODE successfully recognized!");
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Error while parsing next state as an integer!");
            nfe.printStackTrace();
        }
//        System.out.print("Stack after " + lexeme+": ");
//        System.out.println(Arrays.toString(stack.toArray()));
    }

    void reduceStack(int ruleNo) {
        int noOfSymbolsToPop = 2 * (grammar.get(ruleNo).size() - 1);
        if (grammar.get(ruleNo).get(1).equals("''")) noOfSymbolsToPop = 0;
//        System.out.println("No of items to pop = " + noOfSymbolsToPop + ", using rule = " + ruleNo);
        while (noOfSymbolsToPop-- != 0) {
            stack.pop();
        }
//        System.out.println("Stack after reduce1 " + Arrays.toString(stack.toArray()));
        int lastState = Integer.parseInt(stack.peek());
        stack.push(grammar.get(ruleNo).get(0));
//        System.out.println("Stack after reduce2 " + Arrays.toString(stack.toArray()));
        stack.push(table.get(lastState).get(parsingMap.get(grammar.get(ruleNo).get(0))));
//        System.out.println("Stack after reduce3 " + Arrays.toString(stack.toArray()));
        currentState = Integer.parseInt(stack.peek());
    }
}
