import java.util.*;

public class Parser {
    private int currentState;
    private final Stack<Node> stack;
    private final Map<String, String> tokens;
    private final Map<String, Integer> parsingMap;
    private final List<List<String>> table;
    private final List<List<String>> grammar;
    private final Set<String> literalTokens;
    private String prevLexeme;

    Parser() {
        parsingMap = ParseTable.parsingMap;
        table = ParseTable.table;
        grammar = ParseTable.grammar;
        tokens = Tokens.tokens;
        literalTokens = new HashSet<>(Arrays.asList("TK_IDF", "TK_INT", "TK_REAL", "TK_STR"));

        stack = new Stack<>();
        shiftStack("0", true);
    }

    void addLexeme(Pair pair) {
        String lexeme = pair.lexeme;
        String token = pair.token;
        System.out.print("Stack before " + lexeme + ": ");
        System.out.println(Arrays.toString(stack.toArray()));
        String action, symbolToPush = lexeme;
        if (prevLexeme != null && prevLexeme.equals("func") && token.equals("TK_IDF")) {
            symbolToPush = "funcname";
        } else if (tokens.containsKey(lexeme) || token.equals("TK_SAFE")) {
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
                            currentState = getState();
                            action = table.get(currentState).get(parsingMap.get(symbolToPush));
                        }
                        addLexeme(pair);
                        break;
                    case 's':
                        shiftStack(symbolToPush, false);
                        shiftStack(action.substring(1), true);
                        currentState = getState();
                        break;
                    case 'a':
                        System.out.println("CODE successfully parsed!");
                        // Node left in the stack will be the tree node i.e. 'PROGRAM' node
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Error while parsing next state as an integer!");
            nfe.printStackTrace();
        }
        prevLexeme = lexeme;
        System.out.print("Stack after " + lexeme+": ");
        System.out.println(Arrays.toString(stack.toArray()));
    }

    void reduceStack(int ruleNo) {
        int noOfSymbolsToPop = 2 * (grammar.get(ruleNo).size() - 1);
        if (grammar.get(ruleNo).get(1).equals("''")) noOfSymbolsToPop = 0;
//        System.out.println("No of items to pop = " + noOfSymbolsToPop + ", using rule = " + ruleNo);

        List<Node> children = new ArrayList<>();
        while (noOfSymbolsToPop-- != 0) {
            Node poppedItem = stack.pop();
            if (!poppedItem.isState) {
                children.add(poppedItem);
            }
        }

        int lastState = getState();
        Node node = new Node(grammar.get(ruleNo).get(0), false);
        for (Node nd: children) {
            node.addChild(nd);
        }
        shiftStack(node);
        shiftStack(table.get(lastState).get(parsingMap.get(grammar.get(ruleNo).get(0))), true);
    }

    void shiftStack(Node node) {
        stack.push(node);
    }
    void shiftStack(String lexeme, boolean isState) {
        stack.push(new Node (lexeme, isState));
    }

    int getState() {
        return Integer.parseInt(stack.peek().value);
    }
}

class Node {
    private final List<Node> children;
    final String value;
    final boolean isState;

    public Node(String value, boolean isState) {
        this.children = new ArrayList<>();
        this.value = value;
        this.isState = isState;
    }

    public void addChild(Node child) {
        children.add(child);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
