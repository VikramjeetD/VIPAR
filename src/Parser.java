import java.awt.*;
import java.io.*;
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
    private BufferedWriter stackWriter;

    Parser() {
        parsingMap = ParseTable.parsingMap;
        table = ParseTable.table;
        grammar = ParseTable.grammar;
        variableTokens = new HashSet<>(Arrays.asList("TK_IDF", "TK_INT", "TK_REAL", "TK_STR"));
        reduceUntilTokens = new HashSet<>(Arrays.asList(";", "(", "}", "{", "STMT", "STMTLIST", "EXPR", "SUBEXPR"));

        stack = new Stack<>();
        stack.push(new Node("0", true));
        try {
            stackWriter = new BufferedWriter(new FileWriter("src/STACK.txt"));
        } catch (IOException ioe) {
            System.out.println(ANSI_RED + "Unable to open file to write stack states!" + ANSI_RESET);
        }
        writeToStack("Initial", null, false);
    }

    void addLexeme(Pair pair, int line, int pos) {
        String lexeme = pair.lexeme;
        if (errorOccurred) {
            if (lexeme.equals(";") || lexeme.equals("}") || lexeme.equals(")"))
                errorOccurred = false;
                if (lexeme.equals(";")) return;
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
                System.out.println(ANSI_RED + "PARSER ERROR: Unexpected lexeme '" + lexeme + "' at line " + line
                        + ", " + pos + "!\nExpected one of the following: " + getExpectedTokens() + ANSI_RESET);
                errorOccurred = true;
                Node lastPoppedSymbol = stack.pop();
                while (!stack.empty() && !reduceUntilTokens.contains(stack.peek().value)) {
                    lastPoppedSymbol = stack.pop();
                }
                stack.push(lastPoppedSymbol);
            } else {
                switch (action.charAt(0)) {
                    case 'r':
                        reduceStack(Integer.parseInt(action.substring(1)));
                        System.out.println("Applying REDUCE operation for '" + lexeme + "' at line " + line + ", pos " + pos);
                        writeToStack("Reduce", lexeme, false);
                        addLexeme(pair, line, pos);
                        break;
                    case 's':
                        stack.push(new Node(lexeme, false));
                        stack.push(new Node(action.substring(1), true));
                        System.out.println("Applying SHIFT operation for '" + lexeme + "' at line " + line + ", pos " + pos);
                        writeToStack("Shift", lexeme, false);
                        break;
                    case 'a':
                        accepted = true;
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Error while parsing next state as an integer!");
        }

        if (accepted && lexeme.equals("$") && !printed) {
            System.out.println("CODE successfully parsed!");
            writeToStack("Final", null, true);
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
        String nodeClass = node.children == null ? "tf-nc tf-terminal" : node.children.size() == 0 ? "tf-nc null-class" : "tf-nc";
        bw.write("<li>\n" +
                "<span class=\"" + nodeClass + "\">" + node.value + "</span>\n");
        if (node.children != null && node.children.size() > 0) {
            bw.write("<ul>\n");
            for (Node child : node.children) DFSTraversal(child, bw);
            bw.write("</ul>\n");
        }
        bw.write("</li>\n");
    }

    private List<String> getExpectedTokens() {
        Set<Integer> cols = new HashSet<>();
        List<String> row = table.get(getState());
        for (int i = 0; i < row.size(); i++) {
            if (!row.get(i).equals("")) {
                cols.add(i);
            }
        }
        List<String> expectedTokens = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: parsingMap.entrySet()) {
            if (cols.contains(entry.getValue())) {
                expectedTokens.add(entry.getKey());
            }
        }
        return expectedTokens;
    }

    private void writeToStack(String operation, String token, boolean closeFile) {
        String toWrite = "";
        switch (operation) {
            case "Initial":
                toWrite = "Initial Stack: " + stack;
                break;
            case "Reduce":
                toWrite = "REDUCE '" + token + "' -> " + stack;
                break;
            case "Shift":
                toWrite = "SHIFT '" + token + "' -> " + stack;
                break;
            case "Final":
                toWrite = "CODE successfully parsed!";
                break;
        }
        try {
            stackWriter.write(toWrite + "\n");
            if (closeFile)
                stackWriter.close();
        } catch (IOException ignored) {}
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
