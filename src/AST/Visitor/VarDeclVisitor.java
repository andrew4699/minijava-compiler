package AST.Visitor;

import AST.*;
import Semantics.Logger;
import Semantics.VariableTable;

/**
 * Checks that any variable that is being used by the program has first been declared.
 */
public class VarDeclVisitor extends GeneralVisitor {

  private static Visitor v;
  private String methodName;
  private String className;
  private Logger log;

  private VarDeclVisitor() {
    methodName = null;
    className = null;
    log = new Logger(VarDeclVisitor.class);
  }

  public static Visitor getInstance() {
    if (v == null) {
      v = new VarDeclVisitor();
    }
    return v;
  }

  public static void init() {
    v = null;
  }

  private void checkVarDecl(Identifier i) {
    checkVarDecl(i.s, i);
  }

  private void checkVarDecl(IdentifierExp ie) {
    checkVarDecl(ie.s, ie);
  }

  private void checkVarDecl(String varName, ASTNode n) {
    VariableTable methodScope = gst().getClassMethodScope(className, methodName);
    VariableTable classScope = gst().getClassScope(className);

    if (!methodScope.has(varName) && !classScope.has(varName)) {
      String message = String.format(
          "%s undeclared at %s.%s",
          varName,
          className,
          methodName
      );
      log.err(n, message);
    }
  }

  private void setClassName(String name) {
    className = name;
  }

  private void setMethodName(String name) {
    methodName = name;
  }

  @Override
  public void visit(MainClass n) {
    setClassName(n.i1.s);
    n.s.accept(this);
  }

  @Override
  public void visit(ClassDeclSimple n) {
    setClassName(n.i.s);
    visitVariablesAndMethods(n.vl, n.ml);
  }

  @Override
  public void visit(ClassDeclExtends n) {
    setClassName(n.i.s);
    visitVariablesAndMethods(n.vl, n.ml);
  }

  @Override
  public void visit(MethodDecl n) {
    setMethodName(n.i.s);
    n.t.accept(this);

    // visit parameter list
    for (int i = 0; i < n.fl.size(); i++) {
      n.fl.get(i).accept(this);
    }

    // visit variable declarations
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }

    // visit each statement
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
    }

    // print return statement
    n.e.accept(this);
  }

  @Override
  public void visit(Assign n) {
    checkVarDecl(n.i);
    super.visit(n);
  }

  @Override
  public void visit(ArrayAssign n) {
    checkVarDecl(n.i);
    super.visit(n);
  }

  @Override
  public void visit(Call n) {
    n.e.accept(this);
    // skip n.i : the name of the function being called
    for (int i = 0; i < n.el.size(); i++) {
      n.el.get(i).accept(this);
    }
  }

  @Override
  public void visit(NewObject n) {
  }

  @Override
  public void visit(IdentifierExp n) {
    checkVarDecl(n);
    super.visit(n);
  }

  @Override
  public void visit(Identifier n) {
    checkVarDecl(n);
    super.visit(n);
  }
}
