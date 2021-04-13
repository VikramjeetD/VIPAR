import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) throws IOException, InvalidFormatException {
        new Lexer("src/temp2.lang");
    }
}