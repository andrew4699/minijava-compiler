import AST.Program;
import AST.Visitor.ExtendsVisitor;
import org.junit.Test;

public class ExtendsVisitorTest extends  ExpectedOutputTest {

  private static final String TEST_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTED_FILES = TEST_FILES + "Extends/";

  public ExtendsVisitorTest() {
    super(TEST_FILES, EXPECTED_FILES);
  }

  @Override
  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    ExtendsVisitor.init();
    program.accept(ExtendsVisitor.getInstance());
  }

  @Test
  public void extends_main() {
    check("extends_main");
  }

  @Test
  public void extends_self() {
    check("extends_self");
  }

  @Test
  public void extends_unknown_class() {
    check("extends_unknown_class");
  }

  @Test
  public void extends_valid_class() {
    check("extends_valid_class");
  }
}
