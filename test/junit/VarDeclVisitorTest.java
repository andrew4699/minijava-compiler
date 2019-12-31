import AST.Program;
import AST.Visitor.VarDeclVisitor;
import org.junit.Test;

public class VarDeclVisitorTest extends ExpectedOutputTest {

  private static final String TEST_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTED_FILES = TEST_FILES + "VarDecl/";

  public VarDeclVisitorTest() {
    super(TEST_FILES, EXPECTED_FILES);
  }

  @Override
  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    VarDeclVisitor.init();
    program.accept(VarDeclVisitor.getInstance());
  }

  @Test
  public void varsNotDeclared() {
    check("VarsNotDeclared");
  }

  @Test
  public void varsDeclared() {
    check("VarsDeclared");
  }

  @Test
  public void undeclaredVarAsArg() {
    check("UndeclaredVarAsArg");
  }

  @Test
  public void rhsVarNotDeclared() {
    check("RHSVarNotDeclared");
  }

  @Test
  public void varUndeclaredDifferentPlaces() {
    check("VarUndeclaredDifferentPlaces");
  }

  @Test
  public void varsDeclaredInSubclass() {
    check("VarsDeclaredInSubclass");
  }
  
  @Test
  public void varsNotDeclaredInSubclass() {
    check("VarsNotDeclaredInSubclass");
  }
}
