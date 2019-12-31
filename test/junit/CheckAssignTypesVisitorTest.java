import AST.Program;
import AST.Visitor.CheckAssignTypesVisitor;
import AST.Visitor.ComputeTypesVisitor;
import org.junit.Test;

public class CheckAssignTypesVisitorTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "CheckAssignTypes/";

  public CheckAssignTypesVisitorTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    program.accept(CheckAssignTypesVisitor.getInstance());
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void basicBadAssign() {
    check("BasicBadAssign");
  }

  @Test
  public void arrayBadAssign() {
    check("ArrayBadAssign");
  }

  @Test
  public void objectAssign() {
    check("ObjectAssign");
  }
}
