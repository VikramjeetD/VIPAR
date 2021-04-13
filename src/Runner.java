import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

public class Runner {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InvalidFormatException {
        new Lexer("src/code2.lang");
    }
}