import AST.Program;
import AST.Visitor.ClassMethodsVisitor;
import org.junit.Test;

public class ClassMethodsVisitorTest extends  ExpectedOutputTest {

  private static final String TEST_FILES = TestUtils.getTestResources() + "/Semantics/";
  private static final String EXPECTED_FILES = TEST_FILES + "ClassMethods/";

  public ClassMethodsVisitorTest() {
    super(TEST_FILES, EXPECTED_FILES);
  }

  @Override
  public void generateOutput(Program program) {
    computeGlobalSymbolTable(program);
    computeTypes(program);
    ClassMethodsVisitor.init();
    program.accept(ClassMethodsVisitor.getInstance());
  }

  @Test
  public void methodDoesNotExist() {
    check("MethodDoesNotExist");
  }

  @Test
  public void methodsDoExist() {
    check("MethodsDoExist");
  }

  @Test
  public void call_unknown_method_from_main() {
    check("call_unknown_method_from_main");
  }

  @Test
  public void call_unknown_method_from_another_class() {
    check("call_unknown_method_from_another_class");
  }

  @Test
  public void call_known_method_from_another_class() {
    check("call_known_method_from_another_class");
  }

  @Test
  public void valid_many_functions_one_line() {
    check("valid_many_functions_one_line");
  }

  @Test
  public void invalid_many_functions_one_line() {
    check("invalid_many_functions_one_line");
  }

  @Test
  public void call_method_with_correct_num_args() {
    check("call_method_with_correct_num_args");
  }

  @Test
  public void call_method_with_incorrect_num_args() {
    check("call_method_with_incorrect_num_args");
  }

  @Test
  public void invalid_num_args_another_class() {
    check("invalid_num_args_another_class");
  }

  @Test
  public void call_superclass_method_with_correct_args() {
    check("call_superclass_method_with_correct_args");
  }

  @Test
  public void call_superclass_method_with_incorrect_args() {
    check("call_superclass_method_with_incorrect_args");
  }
}
