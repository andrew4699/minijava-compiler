import AST.Program;
import AST.Visitor.GlobalSymbolTableVisitor;
import org.junit.Test;

public class GlobalSymbolTableVisitorTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "GlobalSymbolTable/";

  public GlobalSymbolTableVisitorTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    System.out.println(GlobalSymbolTableVisitor.getInstance().getSymbolTable().toString());
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void declareClassTwice() {
    check("DeclareClassTwice");
  }

  @Test
  public void declareMethodTwice() {
    check("DeclareMethodTwice");
  }

  @Test
  public void declareVariableTwice() {
    check("DeclareVariableTwice");
  }

  @Test
  public void var_method_same() {
    check("var_method_same");
  }
}
