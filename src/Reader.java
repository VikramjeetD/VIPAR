import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
    private final FileReader fileReader;
    Reader(String filepath) throws FileNotFoundException {
        File file = new File(filepath);
        fileReader = new FileReader(file);
    }

    public void readIntoBuffer(char[] buffer) throws IOException {
        fileReader.read(buffer);
    }
}
