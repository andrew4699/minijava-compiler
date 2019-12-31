package AST.Visitor;

import AST.*;
import Semantics.DataTypes.*;
import Semantics.Logger;

/**
 * Verifies each expression has the appropriate type.
 * For example, in the statement
 *   if (expr) stmt
 * expr should be a boolean.
 *
 * Note: this class only checks primitive types of MiniJava,
 * it does not type check user defined objects.
 *
 * @see ClassMethodsVisitor
 * @see ComputeTypesVisitor
 */
public class ExprTypesVisitor extends GeneralVisitor {

  private static Visitor v = null;

  private Logger log;

  public static void init() {
    v = null;
  }

  public static Visitor getInstance() {
    if (v == null) {
      v = new ExprTypesVisitor();
    }
    return v;
  }

  private ExprTypesVisitor() {
    log = new Logger(ExprTypesVisitor.class);
  }

  private void checkType(Exp expr, DataType expectedType) {
    if (!expr.type.equals(expectedType)) {
      String message = String.format(
          "Expression yielded type %s where %s was expected.",
          expr.type.toString(),
          expectedType.toString()
      );
      log.err(expr, message);
    }
  }

  private void checkNumber(Exp e) {
    if (!e.type.equals(IntegerDataType.getInstance()) &&
     !e.type.equals(DoubleDataType.getInstance())) {
      String message = String.format(
          "Expression yielded type %s where number was expected.",
          e.type.toString()
      );
      log.err(e, message);
    }
  }

  @Override
  public void visit(If n) {
    checkType(n.e, BooleanDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(While n) {
    checkType(n.e, BooleanDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(Print n) {
    checkNumber(n.e);
    super.visit(n);
  }

  @Override
  public void visit(Assign n) {
    checkType(n.e, n.i.type);
    super.visit(n);
  }

  @Override
  public void visit(ArrayAssign n) {
    checkType(n.e1, IntegerDataType.getInstance());
    checkType(n.e2, IntegerDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(And n) {
    checkType(n.e1, BooleanDataType.getInstance());
    checkType(n.e2, BooleanDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(LessThan n) {
    checkNumber(n.e2);
    checkType(n.e1, n.e2.type);
    super.visit(n);
  }

  @Override
  public void visit(Plus n) {
    checkNumber(n.e2);
    checkType(n.e1, n.e2.type);
    super.visit(n);
  }

  @Override
  public void visit(Minus n) {
    checkNumber(n.e2);
    checkType(n.e1, n.e2.type);
    super.visit(n);
  }

  @Override
  public void visit(Times n) {
    checkNumber(n.e2);
    checkType(n.e1, n.e2.type);
    super.visit(n);
  }

  @Override
  public void visit(ArrayLookup n) {
    checkType(n.e1, IntegerArrayDataType.getInstance());
    checkType(n.e2, IntegerDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(ArrayLength n) {
    checkType(n.e, IntegerArrayDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(NewArray n) {
    checkType(n.e, IntegerDataType.getInstance());
    super.visit(n);
  }

  @Override
  public void visit(Not n) {
    checkType(n.e, BooleanDataType.getInstance());
    super.visit(n);
  }
}
