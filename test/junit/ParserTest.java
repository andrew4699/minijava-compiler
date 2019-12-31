import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import AST.Program;
import AST.Visitor.ASTVisitor;
import AST.Visitor.PrettyPrintVisitor;
import Parser.parser;
import Scanner.scanner;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import org.junit.After;
import org.junit.Test;

public class ParserTest {

  private final PrintStream outOriginal = System.out;

  private String ppExpected;
  private String astExpected;

  private static final String PARSER_FILES = TestUtils.getTestResources() + "/Parser/";
  private static final String PP_LOCATION = PARSER_FILES + "PrettyPrintVisitor/";
  private static final String AST_LOCATION = PARSER_FILES + "ASTVisitor/";

  /* Turn these flags on or off depending
     on which parser you want to check */
  private static final boolean CHECK_AST = true;
  private static final boolean CHECK_PP = true;

  @After
  public void restoreStreams() {
    System.setOut(outOriginal);
  }

  private void check(String testCaseName) {
    try {
      // config parser
      parser p = setupParser(testCaseName);
      Symbol root = p.parse();
      assertNotNull(root);
      Program program = (Program) root.value;

      // check pretty print and ast visitors
      if (CHECK_PP) {
        ppExpected = getExpected(PP_LOCATION, testCaseName);
        checkPrettyPrintVisitor(program);
      }

      if (CHECK_AST) {
        astExpected = getExpected(AST_LOCATION, testCaseName);
        checkAstVisitor(program);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private parser setupParser(String testCaseName) throws Exception {
    String inputFile = PARSER_FILES + testCaseName + TestUtils.INPUT_EXTENSION;
    File file = new File(inputFile);
    Reader in = new BufferedReader(new FileReader(file));
    ComplexSymbolFactory sf = new ComplexSymbolFactory();
    scanner s = new scanner(in, sf);
    return new parser(s, sf);
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

  // we want to configure system.out so we can compare what was printed
  private OutputStream setupSysOut() {
    OutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    return out;
  }

  private void checkPrettyPrintVisitor(Program program) {
    OutputStream ppContent = setupSysOut();
    program.accept(PrettyPrintVisitor.getInstance());
    assertEquals("PrettyPrintVisitor regression", ppExpected, ppContent.toString());
  }

  private void checkAstVisitor(Program program) {
    OutputStream astContent = setupSysOut();
    program.accept(ASTVisitor.getInstance());
    assertEquals("ASTVisitor regression", astExpected, astContent.toString());
  }

  @Test
  public void foo() {
    check("Foo");
  }

  @Test
  public void crazyBraces() {
    check("CrazyBraces");
  }

  @Test
  public void boolStuff() {
    check("BoolStuff");
  }

  @Test
  public void manyMethods() {
    check("ManyMethods");
  }

  @Test
  public void precedenceFTW() {
    check("PrecedenceFTW");
  }

  @Test
  public void controlFlow() {
    check("ControlFlow");
  }

  @Test
  public void factorial() {
    check("Factorial");
  }

  @Test
  public void bubbleSort() {
    check("BubbleSort");
  }

  @Test
  public void binarySearch() {
    check("BinarySearch");
  }

  @Test
  public void binaryTree() {
    check("BinaryTree");
  }

  @Test
  public void linearSearch() {
    check("LinearSearch");
  }

  @Test
  public void linkedList() {
    check("LinkedList");
  }

  @Test
  public void quickSort() {
    check("QuickSort");
  }

  @Test
  public void treeVisitor() {
    check("TreeVisitor");
  }

  @Test
  public void fieldAndCallPrecedence() {
    check("FieldAndCallPrecedence");
  }

  @Test
  public void fooDouble() {
    check("FooDouble");
  }
}
