import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseTable {
    public static Map<String, Integer> parsingMap; // HashMap from symbols(terminal/non-terminal) -> Table Col No.
    public static List<List<String>> table; // Parse Table, [i][j] -> ith state operation for jth column symbol

    // Grammar Rules with first String always as Left Non-Terminal, and rest as right grammar symbols
    public static List<List<String>> grammar;

    static {
        try {
            fillTable();
            fetchGrammar();
        } catch (IOException | InvalidFormatException e) {
            System.out.println("Unable to extract your grammar!");
            e.printStackTrace();
        }
    }

    // Read Parse Table from 'ParseTable.xlsx'
    private static void fillTable() throws IOException, InvalidFormatException {
        parsingMap = new HashMap<>();
        table = new ArrayList<>();

        XSSFWorkbook wb = new XSSFWorkbook(new File("src/ParseTable.xlsx"));
        XSSFSheet sheet = wb.getSheetAt(0);
        DataFormatter df = new DataFormatter();

        int colNo = 0;
        for (Cell cell: sheet.getRow(0))
            parsingMap.put(df.formatCellValue(cell), colNo++);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            table.add(new ArrayList<>());
            for (Cell cell: sheet.getRow(i)) {
                table.get(i-1).add(df.formatCellValue(cell));
            }
        }
    }

    // Read grammar rules from 'Grammar.txt'
    private static void fetchGrammar() throws IOException {
        grammar = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader("src/Grammar.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                grammar.add(new ArrayList<>());
                for (String str: line.split(" ")) {
                    if (!str.equals("->"))
                        grammar.get(grammar.size()-1).add(str);
                }
            }
        }
    }
}
