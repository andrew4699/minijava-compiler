package AST.Visitor;

import AST.Call;
import Semantics.DataTypes.UnknownDataType;
import Semantics.Logger;
import Semantics.MethodInfo;

public class ClassMethodsVisitor extends GeneralVisitor {

  private static Visitor v = null;

  private Logger log;

  private ClassMethodsVisitor() {
    log = new Logger(ClassMethodsVisitor.class);
  }

  public static Visitor getInstance() {
    if (v == null) {
      v = new ClassMethodsVisitor();
    }
    return v;
  }

  public static void init() {
    v = null;
  }

  @Override
  public void visit(Call n) {
    String className = n.e.type.toString();
    String methodName = n.i.s;

    StringBuilder sb = new StringBuilder();

    sb.append(methodName).append('(');
    for (int i = 0; i < n.el.size(); i++) {
      sb.append(n.el.get(i).type.toString());
      if (i + 1 < n.el.size()) {
        sb.append(", ");
      }
    }
    sb.append(')');

    if (n.type instanceof UnknownDataType) {
      log.err(n.i, sb.toString() + " does not belong to class " + className);
    }

    super.visit(n);
  }
}
