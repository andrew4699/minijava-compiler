import AST.Program;
import AST.Visitor.ComputeTypesVisitor;
import org.junit.Test;

public class ComputeTypesVisitorTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "ComputeTypes/";

  public ComputeTypesVisitorTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    System.out.print(ComputeTypesVisitor.getInstance().toString());
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void UndeclaredClass() {
    check("UndeclaredClass");
  }
}
