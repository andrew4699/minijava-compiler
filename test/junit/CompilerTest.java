import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import AST.Program;
import Scanner.scanner;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import Parser.*;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import org.junit.After;
import org.junit.Ignore;

@Ignore
public abstract class CompilerTest {

  @After
  public void restoreStreams() {
    System.setOut(System.out);
  }

  protected parser setupParser(String fileDir, String testCaseName) throws Exception {
    String inputFile = fileDir + testCaseName + TestUtils.INPUT_EXTENSION;
    File file = new File(inputFile);
    Reader in = new BufferedReader(new FileReader(file));
    ComplexSymbolFactory sf = new ComplexSymbolFactory();
    scanner s = new scanner(in, sf);
    return new parser(s, sf);
  }

  // we want to configure system.out so we can compare what was printed
  protected OutputStream setupSysOut() {
    OutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    return out;
  }
}
