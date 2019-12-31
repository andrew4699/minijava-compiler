import AST.Program;
import AST.Visitor.CheckMainVisitor;
import org.junit.Test;

public class CheckMainVisitorTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "CheckMain/";

  public CheckMainVisitorTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    program.accept(CheckMainVisitor.getInstance());
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void badMainArgumentType() {
    check("BadMainArgumentType");
  }

  @Test
  public void badMainMethodName() {
    check("BadMainMethodName");
  }
}
