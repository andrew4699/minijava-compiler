import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import AST.Program;
import AST.Visitor.*;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;
import Scanner.scanner;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import Parser.*;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import org.junit.After;
import org.junit.Ignore;

@Ignore
public abstract class ExpectedOutputTest extends CompilerTest {

  private final PrintStream outOriginal = System.out;

  private String fileDir;
  private String expectationDir;

  public ExpectedOutputTest(String fileDir, String expectationDir) {
    this.fileDir = fileDir;
    this.expectationDir = expectationDir;
  }

  public abstract void generateOutput(Program program);

  protected GlobalSymbolTable getGlobalSymbolTable() {
    return GlobalSymbolTableVisitor.getInstance().getSymbolTable();
  }

  protected void computeGlobalSymbolTable(Program program) {
    GlobalSymbolTableVisitor.getInstance().clearSymbolTable();
    program.accept(GlobalSymbolTableVisitor.getInstance());
  }

  protected void computeTypes(Program program) {
    ComputeTypesVisitor.getInstance().clear();
    program.accept(ComputeTypesVisitor.getInstance());
  }

  protected void check(String testCaseName) {
    try {
      // config parser
      parser p = setupParser(fileDir, testCaseName);
      Symbol root = p.parse();
      assertNotNull(root);
      Program program = (Program) root.value;

      // check visitors
      String expected = getExpected(expectationDir, testCaseName);
      OutputStream actual = setupSysOut();
      generateOutput(program);
      assertEquals("regression", expected, actual.toString());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private String getExpected(String directory, String fileName) throws Exception {
    return Files.readString(
        Paths.get(
            directory,
            fileName + TestUtils.EXPECTED_EXTENSION
        ),
        Charset.defaultCharset()
    );
  }
}
