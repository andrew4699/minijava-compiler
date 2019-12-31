package AST.Visitor;

import AST.ClassDeclExtends;
import Semantics.DataTypes.UnknownDataType;
import Semantics.Logger;
import Semantics.MethodInfo;

public class ExtendsVisitor extends GeneralVisitor {

  private static Visitor v = null;

  private Logger log;

  private ExtendsVisitor() {
    log = new Logger(ExtendsVisitor.class);
  }

  public static Visitor getInstance() {
    if (v == null) {
      v = new ExtendsVisitor();
    }
    return v;
  }

  public static void init() {
    v = null;
  }

  @Override
  public void visit(ClassDeclExtends n) {
    String selfClassName = n.i.s;
    String extendsClassName = n.j.s;

    if (selfClassName.equals(extendsClassName)) {
      log.err(n, selfClassName + " cannot extend itself");
    }

    if (gst().getClassInfo(extendsClassName) == null) {
      log.err(n, selfClassName + " extends an unknown class " + extendsClassName);
    }

    super.visit(n);
  }
}
