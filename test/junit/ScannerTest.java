import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import Parser.sym;
import Scanner.scanner;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import org.junit.Ignore;
import org.junit.Test;

/*
    This class shows one way to use JUnit for testing your compiler.

    NOTE: The single provided test case here is not designed for MiniJava!
    Use this as a starting point, but you will want to create tests
    that fit the MiniJava grammar and match whatever output format you choose
    (e.g. your chosen token names, parse table formats, etc).
    In later phases of the project, you may find it helpful to write test
    cases for Minijava.java itself rather than the underlying modules as is
    shown here.
*/
public class ScannerTest {

  private static final String TEST_FILES_LOCATION =
      TestUtils.getTestResources() + "/Scanner/";

  /*
      You may be able to reuse this private helper method for your own
      testing of the MiniJava scanner.
  */
  private void check(String testCaseName) {
    try {
      FileInputStream input = new FileInputStream(
          TEST_FILES_LOCATION + testCaseName + TestUtils.INPUT_EXTENSION
      );

      // get all the tokens from the .expected file
      String[] expected = Files.readString(
          Paths.get(
              TEST_FILES_LOCATION,
              testCaseName + TestUtils.EXPECTED_EXTENSION
          ),
          Charset.defaultCharset()
      ).split("\\s+");

      // make sure expected only contains nonempty strings
      // we need this because an empty file produces [""] which
      // we don't want.
      expected = Arrays
          .stream(expected)
          .filter(str -> !str.isBlank())
          .toArray(String[]::new);

      ComplexSymbolFactory sf = new ComplexSymbolFactory();
      Reader in = new BufferedReader(new InputStreamReader(input));
      scanner s = new scanner(in, sf);
      Symbol t = s.next_token();
      int i = 0;
      while (t.sym != sym.EOF) {
        // verify each token that we scan
        assertEquals(expected[i], s.symbolToString(t));
        t = s.next_token();
        i++;
      }

      // Check that the number of input & expected tokens is the same
      assertEquals(i, expected.length);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void onlyComments() {
    check("OnlyComments");
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void basicMainMethod() {
    check("BasicMainMethod");
  }

  @Test
  public void basicMainMethodCommented() {
    check("BasicMainMethodCommented");
  }

  @Test
  public void basicMainMethodBlockComments() {
    check("BasicMainMethodBlockComments");
  }

  @Test
  public void blockComments() {
    check("BlockComments");
  }

  @Test
  public void bracesParenthesisBrackets() {
    check("BracesParenthesisBracketsOhMy");
  }

  @Test
  public void operators() {
    check("Operators");
  }

  @Test
  public void booleanExpression() {
    check("BooleanExpression");
  }

  @Test
  public void controlFlow() {
    check("ControlFlow");
  }

  @Test
  public void array() {
    check("Array");
  }

  @Test
  public void argList() {
    check("ArgumentList");
  }

  @Test
  public void createNewArray() {
    check("CreateNewArray");
  }


  @Test
  public void illegalIdentifier() {
    check("IllegalIdentifier");
  }

  @Test
  public void crazyIdentifier() {
    check("CrazyIdentifier");
  }

  @Test
  public void garbage() {
    check("Garbage");
  }

  @Test
  public void minified() {
    check("Minified");
  }

  @Test
  public void basicDoubles() {
    check("BasicDoubles");
  }
}
