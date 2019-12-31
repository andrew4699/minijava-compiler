import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import AST.Program;
import AST.Visitor.CodeGenVisitor;
import AST.Visitor.ComputeTypesVisitor;
import AST.Visitor.GlobalSymbolTableVisitor;
import Parser.parser;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java_cup.runtime.Symbol;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public class CodeGenTest extends CompilerTest {

  private static final String CODEGEN_FILES = TestUtils.getTestResources() + "/CodeGen/";


  protected void check(String testCaseName) {
    try {
      String expected = getJavaOutput(testCaseName);
      String asm = generateASM(testCaseName);
      String actual = getASMOutput(asm, testCaseName);
      assertEquals("regression", expected, actual);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  protected void checkRuntimeError(String testCaseName) {
    try {
      String asm = generateASM(testCaseName);
      String actual = getASMOutput(asm, testCaseName);
      assertEquals("ArrayIndexOutOfBoundsException\n", actual);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }


  private String generateASM(String testCaseName) throws Exception {
    parser p = setupParser(CODEGEN_FILES, testCaseName);
    Symbol root = p.parse();
    assertNotNull(root);

    GlobalSymbolTableVisitor gstVisitor = GlobalSymbolTableVisitor.getInstance();
    gstVisitor.clearSymbolTable();

    ComputeTypesVisitor ctVisitor = ComputeTypesVisitor.getInstance();
    ctVisitor.clear();

    CodeGenVisitor.init();
    CodeGenVisitor cgVisitor = CodeGenVisitor.getInstance();

    Program program = (Program)root.value;
    program.accept(gstVisitor);
    program.accept(ctVisitor);
    program.accept(cgVisitor);
    return cgVisitor.toString();
  }

  private String getJavaOutput(String testCaseName) throws Exception {
    String dir = TestUtils.getProjectDir() + "test/resources/CodeGen";
    exec("javac -d " + dir + "/out " + testCaseName + ".java", dir);
    String res = exec("java " + testCaseName, dir + "/out");
    return res;
  }

  private String exec(String cmd, String dir) throws Exception {
    Process pr = Runtime.getRuntime().exec(cmd, null, new File(dir));

    BufferedReader inputReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    BufferedReader errorReader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

    StringBuffer output = new StringBuffer();
    StringBuffer error = new StringBuffer();
    String line = null;

    while ((line = inputReader.readLine()) != null) {
      output.append(line);
      output.append("\n");
    }

    while ((line = errorReader.readLine()) != null) {
      error.append(line);
      error.append("\n");
    }

    inputReader.close();
    errorReader.close();

    int exitCode = pr.waitFor();
    assertEquals(error.toString(), 0, exitCode);
    return output.toString();
  }

  private String getASMOutput(String asm, String testCaseName) throws Exception {
    String projectDir = TestUtils.getProjectDir();
    String runtimeDir = projectDir + "src/runtime";
    String asmFile = testCaseName + ".s";
    String outFile = "./" + testCaseName;

    Files.write(Paths.get(runtimeDir + "/" + asmFile), asm.getBytes());

    String[] compile = { "gcc", "-no-pie", "-o", outFile, asmFile, "boot.c",
        "number_converter.c",
    "-lm" };
    exec(String.join(" ", compile), runtimeDir);

    return exec(outFile, runtimeDir);
  }

  @Test
  public void PrintIntegerExprs() {
    check("PrintIntegerExprs");
  }

  @Test
  public void DoubleMethodArgs() {
    check("DoubleMethodArgs");
  }

  @Test
  public void PrintDoubleExprs() {
    check("PrintDoubleExprs");
  }

  @Test
  public void DoubleExpressions() {
    check("DoubleExpressions");
  }

  @Test
  public void SimpleClassMethod() {
    check("SimpleClassMethod");
  }

  @Test
  public void SimpleClassMultiMethod() {
    check("SimpleClassMultiMethod");
  }

  @Test
  public void SimpleTwoClassMethods() {
    check("SimpleTwoClassMethods");
  }

  @Test
  public void ComplexClassPrintIntegerExprs() {
    check("ComplexClassPrintIntegerExprs");
  }

  @Test
  public void SimpleField() {
    check("SimpleField");
  }

  @Test
  public void SimpleTwoClassField() {
    check("SimpleTwoClassField");
  }

  @Test
  public void PrintOverAndOver() {
    check("PrintOverAndOver");
  }

  @Test
  public void MethodCallAsParam() {
    check("MethodCallAsParam");
  }

  @Test
  public void IntMethodArgs() {
    check("IntMethodArgs");
  }

  @Test
  public void SmallIntMethodArgs() {
    check("SmallIntMethodArgs");
  }

  @Test
  public void CanPrintIntExpressions() {
    check("CanPrintIntExpressions");
  }

  @Test
  public void CanUseLocalVars() {
    check("CanUseLocalVars");
  }

  @Test
  public void CanUseLocalsWithFields() {
    check("CanUseLocalsWithFields");
  }

  @Test
  public void CanUseLocalsWithFieldsEasy() {
    check("CanUseLocalsWithFieldsEasy");
  }

  @Test
  public void CanUseIfElse() {
    check("CanUseIfElse");
  }

  @Test
  public void CanDoWhileLoops() {
    check("CanDoWhileLoops");
  }

  @Test
  public void CanDoControlFlow() {
    check("CanDoControlFlow");
  }

  @Test
  public void CanMakeSimpleSuperClass() {
    check("CanMakeSimpleSuperClass");
  }

  @Test
  public void CanDoComplexSuperClasses() {
    check("CanDoComplexSuperClasses");
  }

  @Test
  public void CanDoLocalObjects() {
    check("CanDoLocalObjects");
  }

  @Test
  public void CanDoMethodsNotOverridden() {
    check("CanDoMethodsNotOverridden");
  }

  @Test
  public void PassObjectAsParam() {
    check("PassObjectAsParam");
  }

  @Test
  public void CanUseArrays() {
    check("CanUseArrays");
  }

  @Test
  public void NewArray() {
    check("NewArray");
  }

  @Test
  public void AccessArray() {
    check("AccessArray");
  }

  @Test
  public void ComplexArrays() {
    check("ComplexArrays");
  }

  @Test
  public void AssignBoolean() {
    check("AssignBoolean");
  }

  @Test
  public void Booleans() {
    check("Booleans");
  }

  @Test
  public void BinarySearch() {
    check("BinarySearch");
  }

  @Test
  public void BinaryTree() {
    check("BinaryTree");
  }

  @Test
  public void BubbleSort() {
    check("BubbleSort");
  }

  @Test
  public void Factorial() {
    check("Factorial");
  }

  @Test
  public void LinearSearch() {
    check("LinearSearch");
  }

  @Test
  public void LinkedList() {
    check("LinkedList");
  }

  @Test
  public void UsingThis() {
    check("UsingThis");
  }

  @Test
  public void QuickSort() {
    check("QuickSort");
  }

  @Test
  public void TreeVisitor() {
    check("TreeVisitor");
  }

  @Test
  public void InheritFields() {
    check("InheritFields");
  }

  @Test
  public void ThisAsArg() {
    check("ThisAsArg");
  }

  @Test
  public void ShortCircuitAnd() {
    check("ShortCircuitAnd");
  }

  @Test
  public void OutOfBounds() {
    checkRuntimeError("OutOfBounds");
  }

  @Test
  public void ClassWithDoubles() {
    check("ClassWithDoubles");
  }

  @Test
  public void CanUseDoubleLocalVars() {
    check("CanUseDoubleLocalVars");
  }

  @Test
  public void CanUseDoubleLocalsWithFields() {
    check("CanUseDoubleLocalsWithFields");
  }

  @Test
  public void CanDoDoublesControlFlow() {
    check("CanDoDoublesControlFlow");
  }
}
