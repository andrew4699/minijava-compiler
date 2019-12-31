import AST.Program;
import AST.Visitor.ReturnTypeVisitor;
import AST.Visitor.VarDeclVisitor;
import org.junit.Test;

public class ReturnTypeVisitorTest extends ExpectedOutputTest {

  private static final String TEST_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTED_FILES = TEST_FILES + "ReturnType/";

  public ReturnTypeVisitorTest() {
    super(TEST_FILES, EXPECTED_FILES);
  }

  @Override
  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    ReturnTypeVisitor.init();
    program.accept(ReturnTypeVisitor.getInstance());
  }

  @Test
  public void check_return_type() {
    check("check_return_type");
  }
}
