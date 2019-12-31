import AST.Program;
import org.junit.Test;

public class CheckOverrideSignatureTest extends ExpectedOutputTest {

  private static final String SEMANTICS_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTATION_LOCATION = SEMANTICS_FILES + "CheckOverrideSignature/";

  public CheckOverrideSignatureTest() {
    super(SEMANTICS_FILES, EXPECTATION_LOCATION);
  }

  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    getGlobalSymbolTable().logOverrideSignatureMismatches();
  }

  @Test
  public void basicClass() {
    check("BasicClass");
  }

  @Test
  public void basicOverride() {
    check("BasicOverride");
  }

  @Test
  public void overrideArgumentMismatch() {
    check("OverrideArgumentMismatch");
  }

  @Test
  public void overrideReturnMismatch() {
    check("OverrideReturnMismatch");
  }
}
