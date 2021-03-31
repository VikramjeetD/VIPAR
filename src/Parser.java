import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Parser {
    private final Stack<Node> stack;
    private final Map<String, Integer> parsingMap;
    private final List<List<String>> table;
    private final List<List<String>> grammar;
    private final Set<String> variableTokens;
    private final Set<String> reduceUntilTokens;
    private boolean errorOccurred, accepted, printed;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    Parser() {
        parsingMap = ParseTable.parsingMap;
        table = ParseTable.table;
        grammar = ParseTable.grammar;
        variableTokens = new HashSet<>(Arrays.asList("TK_IDF", "TK_INT", "TK_REAL", "TK_STR"));
        reduceUntilTokens = new HashSet<>(Arrays.asList(";", "}", "{"));

        stack = new Stack<>();
        stack.push(new Node("0", true));
        System.out.println("Initial stack: " + stack);
    }

    void addLexeme(Pair pair) {
        String lexeme = pair.lexeme;
        if (errorOccurred) {
            if (lexeme.equals(";") || lexeme.equals("}"))
                errorOccurred = false;
            return;
        }

        String action, parseSymbol, token = pair.token;
        if (lexeme.equals("->")) {
            parseSymbol = "arrow";
        } else if (variableTokens.contains(token)) {
            parseSymbol = "variable";
        } else {
            parseSymbol = lexeme;
        }

        action = table.get(getState()).get(parsingMap.get(parseSymbol));
        try {
            if (action.length() == 0) {
                System.out.print(ANSI_RED + "Unexpected lexeme: '" + lexeme + "' ! ");
                errorOccurred = true;
                Node lastPoppedSymbol = stack.peek();
                while (!stack.empty() && !reduceUntilTokens.contains(stack.peek().value)) {
                    lastPoppedSymbol = stack.pop();
                }
                stack.push(lastPoppedSymbol);
                System.out.println("Reduced Stack: " + stack + ANSI_RESET);
            } else {
                switch (action.charAt(0)) {
                    case 'r':
                        reduceStack(Integer.parseInt(action.substring(1)));
                        System.out.println("Stack after applying REDUCE operation for '" + lexeme + "' : " + stack);
                        addLexeme(pair);
                        break;
                    case 's':
                        stack.push(new Node(lexeme, false));
                        stack.push(new Node(action.substring(1), true));
                        System.out.println("Stack after applying SHIFT operation for '" + lexeme + "' : " + stack);
                        break;
                    case 'a':
                        accepted = true;
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Error while parsing next state as an integer!");
            nfe.printStackTrace();
        }

        if (accepted && lexeme.equals("$") && !printed) {
            System.out.println("CODE successfully parsed!");
            printed = true;
            drawTree();
        }
    }

    private void reduceStack(int ruleNo) {
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
        node.addChildren(children);
        stack.push(node);
        stack.push(new Node(table.get(lastState).get(parsingMap.get(grammar.get(ruleNo).get(0))), true));
    }

    private int getState() {
        return Integer.parseInt(stack.peek().value);
    }

    private void drawTree() {
        try {
            File file = new File("src/tree.html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    " <meta charset=\"UTF-8\">\n" +
                    " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    " <title>Syntax Tree</title>\n" +
                    " <link rel=\"stylesheet\" href=\"./tree.css\">\n" +
                    " </head>\n" +
                    "<body>\n<div class=\"tf-tree\">\n<ul>\n");

            stack.pop();
            Node root = stack.pop();
            DFSTraversal(root, bw);

            bw.write("</ul>\n</div>\n</body>\n</html>");
            bw.close();
            Desktop.getDesktop().browse(file.toURI());
        } catch (IOException ioe) {
            System.out.println("Unable to draw Tree!");
            ioe.printStackTrace();
        }
    }

    private void DFSTraversal(Node node, BufferedWriter bw) throws IOException {
        String nodeClass = (node.children != null && node.children.size() == 0) ? "tf-nc null-class" : "tf-nc";
        bw.write("<li>\n" +
                "<span class=\"" + nodeClass + "\">" + node.value + "</span>\n");
        if (node.children != null && node.children.size() > 0) {
            bw.write("<ul>\n");
            for (Node child : node.children) DFSTraversal(child, bw);
            bw.write("</ul>\n");
        }
        bw.write("</li>\n");
    }
}

class Node {
    List<Node> children;
    final String value;
    final boolean isState;

    public Node(String value, boolean isState) {
        this.value = value;
        this.isState = isState;
        children = null;
    }

    public void addChildren(List<Node> children) {
        Collections.reverse(children);
        this.children = children;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
