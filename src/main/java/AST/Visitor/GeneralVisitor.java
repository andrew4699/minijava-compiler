package AST.Visitor;

import AST.*;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;

public abstract class GeneralVisitor implements Visitor {

  protected GlobalSymbolTable gst() {
    return GlobalSymbolTableVisitor.getInstance().getSymbolTable();
  }

  @Override
  public void visit(Program n) {
    n.m.accept(this);
    for (int i = 0; i < n.cl.size(); i++) {
      n.cl.get(i).accept(this);
    }
  }

  @Override
  public void visit(MainClass n) {
    n.i1.accept(this);
    n.s.accept(this);
  }

  @Override
  public void visit(ClassDeclSimple n) {
    n.i.accept(this);
    visitVariablesAndMethods(n.vl, n.ml);
  }

  protected void visitVariablesAndMethods(VarDeclList vl, MethodDeclList ml) {
    for (int i = 0; i < vl.size(); i++) {
      vl.get(i).accept(this);
    }

    for (int i = 0; i < ml.size(); i++) {
      ml.get(i).accept(this);
    }
  }

  @Override
  public void visit(ClassDeclExtends n) {
    n.i.accept(this);
    n.j.accept(this);

    visitVariablesAndMethods(n.vl, n.ml);
  }

  @Override
  public void visit(VarDecl n) {
    n.t.accept(this);
    n.i.accept(this);
  }

  @Override
  public void visit(MethodDecl n) {
    n.i.accept(this);
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
  public void visit(Formal n) {
    n.t.accept(this);
    n.i.accept(this);
  }

  @Override
  public void visit(IntArrayType n) {
  }

  @Override
  public void visit(BooleanType n) {
  }

  @Override
  public void visit(IntegerType n) {
  }

  @Override
  public void visit(DoubleType n) {
  }

  @Override
  public void visit(IdentifierType n) {
  }

  @Override
  public void visit(Block n) {
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
    }
  }

  @Override
  public void visit(If n) {
    n.e.accept(this);
    n.s1.accept(this);
    n.s2.accept(this);
  }

  @Override
  public void visit(While n) {
    n.e.accept(this);
    n.s.accept(this);
  }

  @Override
  public void visit(Print n) {
    n.e.accept(this);
  }

  private void printExpression(Exp e) {
    e.accept(this);
  }

  @Override
  public void visit(Assign n) {
    n.i.accept(this);
    n.e.accept(this);
  }

  @Override
  public void visit(ArrayAssign n) {
    n.i.accept(this);
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(And n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(LessThan n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(Plus n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(Minus n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(Times n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(ArrayLookup n) {
    n.e1.accept(this);
    n.e2.accept(this);
  }

  @Override
  public void visit(ArrayLength n) {
    n.e.accept(this);
  }

  @Override
  public void visit(Call n) {
    n.e.accept(this);
    n.i.accept(this);

    for (int i = 0; i < n.el.size(); i++) {
      n.el.get(i).accept(this);
    }
  }

  @Override
  public void visit(IntegerLiteral n) {
  }

  @Override
  public void visit(DoubleLiteral n) {
  }

  @Override
  public void visit(True n) {
  }

  @Override
  public void visit(False n) {
  }

  @Override
  public void visit(IdentifierExp n) {
  }

  @Override
  public void visit(This n) {
  }

  @Override
  public void visit(NewArray n) {
    n.e.accept(this);
  }

  @Override
  public void visit(NewObject n) {
    n.i.accept(this);
  }

  @Override
  public void visit(Not n) {
    n.e.accept(this);
  }

  @Override
  public void visit(Identifier n) {
  }
}
