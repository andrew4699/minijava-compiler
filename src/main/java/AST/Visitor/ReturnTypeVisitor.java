package AST.Visitor;

import AST.*;
import Semantics.ClassInfo;
import Semantics.DataTypes.*;
import Semantics.Logger;
import java.util.Collections;

public class ReturnTypeVisitor extends GeneralVisitor {

  private static Visitor v;
  private String methodName;
  private String className;
  private Logger log;

  private ReturnTypeVisitor() {
    methodName = null;
    className = null;
    log = new Logger(ReturnTypeVisitor.class);
  }

  public static Visitor getInstance() {
    if (v == null) {
      v = new ReturnTypeVisitor();
    }
    return v;
  }

  public static void init() {
    v = null;
  }

  private void checkTypes(ASTNode n, DataType exp, DataType act) {
    if (!act.isAssignableToType(exp)) {
      error(n, exp, act);
    }
  }

  private void error(ASTNode n, DataType exp, DataType act) {
    String msg = String.format(
        "%s.%s returned %s but expects %s",
        className,
        methodName,
        act.toString(),
        exp.toString()
    );
    log.err(n, msg);
  }

  @Override
  public void visit(ClassDeclSimple n) {
    className = n.i.s;
    super.visit(n);
    className = null;
  }

  @Override
  public void visit(ClassDeclExtends n) {
    className = n.i.s;
    super.visit(n);
    className = null;
  }

  @Override
  public void visit(MethodDecl n) {
    methodName = n.i.s;
    DataType typeExpected = gst()
        .getClassMethodTable(className)
        .getMethodInfo(methodName)
        .returnType;
    DataType typeReturned = n.e.type;
    checkTypes(n, typeExpected, typeReturned);
    methodName = null;
  }

}
