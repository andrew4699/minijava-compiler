import AST.Program;
import org.junit.Test;

public class CheckInheritanceCyclesTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "CheckInheritanceCycles/";

  public CheckInheritanceCyclesTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    getGlobalSymbolTable().logInheritanceCycles();
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void cyclicClass() {
    check("CyclicClass");
  }

  @Test
  public void long_cycle() {
    check("long_cycle");
  }
}
