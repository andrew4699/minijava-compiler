import AST.Program;
import AST.Visitor.ClassMethodsVisitor;
import AST.Visitor.ExprTypesVisitor;
import org.junit.Test;

public class ExprTypesVisitorTest extends ExpectedOutputTest {

  public static final String TEST_FILES = TestUtils.getTestResources() + "/Semantics/";
  public static final String EXPECTED_FILES = TEST_FILES + "ExprTypes/";

  public ExprTypesVisitorTest() {
    super(TEST_FILES, EXPECTED_FILES);
  }

  @Override
  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    ExprTypesVisitor.init();
    program.accept(ExprTypesVisitor.getInstance());
  }

  @Test
  public void check_arithmetic_ops() {
    check("check_arithmetic_ops");
  }

  @Test
  public void check_boolean_expressions() {
    check("check_boolean_expressions");
  }

  @Test
  public void check_array_expr_types() {
    check("check_array_expr_types");
  }

  @Test
  public void check_print_expr_type() {
    check("check_print_expr_type");
  }
}
