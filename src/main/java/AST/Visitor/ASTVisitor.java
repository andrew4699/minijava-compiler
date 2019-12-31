package AST.Visitor;

import AST.*;

public class ASTVisitor implements Visitor {

  private static ASTVisitor singletonVisitor = null;

  private int tabs;

  private ASTVisitor() {
    tabs = 0;
  }

  public static ASTVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new ASTVisitor();
    }
    return singletonVisitor;
  }

  // prints indentation
  private void indent() {
    for (int i = 0; i < this.tabs; i++) {
      System.out.print("  ");
    }
  }

  // prints line number followed by a new line
  private void lineNumber(ASTNode n) {
    System.out.printf(" (line %d)", n.line_number);
    System.out.println();
  }

  @Override
  public void visit(Program n) {
    System.out.println("Program");
    n.m.accept(this);
    for (int i = 0; i < n.cl.size(); i++) {
      n.cl.get(i).accept(this);
    }
  }

  @Override
  public void visit(MainClass n) {
    tabs++;
    indent();

    // print main class info
    System.out.print("MainClass ");
    n.i1.accept(this);
    lineNumber(n.i1);

    // print main class statement
    tabs++;
    n.s.accept(this);
    System.out.println();
    tabs--;

    tabs--;
  }

  @Override
  public void visit(ClassDeclSimple n) {
    tabs++;
    indent();

    System.out.print("Class ");
    n.i.accept(this);
    lineNumber(n.i);
    printVariablesAndMethods(n.vl, n.ml);

    tabs--;
  }

  private void printVariablesAndMethods(VarDeclList vl, MethodDeclList ml) {
    tabs++;
    for (int i = 0; i < vl.size(); i++) {
      vl.get(i).accept(this);
    }
    tabs--;
    for (int i = 0; i < ml.size(); i++) {
      ml.get(i).accept(this);
    }
  }

  @Override
  public void visit(ClassDeclExtends n) {
    tabs++;
    indent();

    System.out.print("Class ");
    n.i.accept(this);
    System.out.print(" extends ");
    n.j.accept(this);
    lineNumber(n.j);

    printVariablesAndMethods(n.vl, n.ml);

    tabs--;
  }

  @Override
  public void visit(VarDecl n) {
    indent();

    System.out.print("VarDecl ");
    n.t.accept(this);
    System.out.print(" ");
    n.i.accept(this);
    lineNumber(n);
  }

  @Override
  public void visit(MethodDecl n) {
    tabs++;
    indent();

    System.out.print("MethodDecl ");
    n.i.accept(this);
    lineNumber(n);

    // prints return type
    tabs++;
    indent();
    System.out.print("returns ");
    n.t.accept(this);
    System.out.println();

    // prints parameter list
    if (n.fl.size() > 0) {
      printParameters(n);
    }

    // print variable declarations
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }

    // print each statement
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
      System.out.println();
    }

    // print return statement
    indent();
    System.out.print("Return ");
    n.e.accept(this);
    lineNumber(n.e);

    tabs--;
    tabs--;
  }

  private void printParameters(MethodDecl n) {
    indent();
    System.out.println("parameters:");
    tabs++;
    for (int i = 0; i < n.fl.size(); i++) {
      indent();
      n.fl.get(i).accept(this);
      System.out.println();
    }
    tabs--;
  }

  @Override
  public void visit(Formal n) {
    n.t.accept(this);
    System.out.print(" ");
    n.i.accept(this);
  }

  @Override
  public void visit(IntArrayType n) {
    System.out.print("int[]");
  }

  @Override
  public void visit(BooleanType n) {
    System.out.print("boolean");
  }

  @Override
  public void visit(IntegerType n) {
    System.out.print("int");
  }

  @Override
  public void visit(DoubleType n) {
    System.out.print("double");
  }

  @Override
  public void visit(IdentifierType n) {
    System.out.print(n.s);
  }

  @Override
  public void visit(Block n) {
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);

      // don't start a new line on the last statement
      if (i + 1 < n.sl.size()) {
        System.out.println();
      }
    }
  }

  @Override
  public void visit(If n) {
    indent();
    System.out.print("If ");
    n.e.accept(this);
    lineNumber(n.e);

    tabs++;
    n.s1.accept(this);
    lineNumber(n.s1);
    tabs--;

    indent();
    System.out.println("Else");
    tabs++;
    n.s2.accept(this);
    tabs--;
  }

  @Override
  public void visit(While n) {
    indent();
    System.out.print("While ");
    n.e.accept(this);
    lineNumber(n.e);

    tabs++;
    n.s.accept(this);
    tabs--;
  }

  @Override
  public void visit(Print n) {
    indent();
    System.out.print("Print");
    lineNumber(n.e);

    // what is being printed?
    printExpression(n.e);
  }

  private void printExpression(Exp e) {
    tabs++;
    indent();
    e.accept(this);
    tabs--;
  }

  @Override
  public void visit(Assign n) {
    indent();
    n.i.accept(this);
    System.out.print(" = ");
    n.e.accept(this);
  }

  @Override
  public void visit(ArrayAssign n) {
    indent();
    n.i.accept(this);
    System.out.print("[");
    n.e1.accept(this);
    System.out.print("] = ");
    n.e2.accept(this);
  }

  @Override
  public void visit(And n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" && ");
    n.e2.accept(this);
    System.out.print(")");
  }

  @Override
  public void visit(LessThan n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" < ");
    n.e2.accept(this);
    System.out.print(")");
  }

  @Override
  public void visit(Plus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" + ");
    n.e2.accept(this);
    System.out.print(")");
  }

  @Override
  public void visit(Minus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" - ");
    n.e2.accept(this);
    System.out.print(")");
  }

  @Override
  public void visit(Times n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" * ");
    n.e2.accept(this);
    System.out.print(")");
  }

  @Override
  public void visit(ArrayLookup n) {
    n.e1.accept(this);
    System.out.print("[");
    n.e2.accept(this);
    System.out.print("]");
  }

  @Override
  public void visit(ArrayLength n) {
    n.e.accept(this);
    System.out.print(".length");
  }

  @Override
  public void visit(Call n) {
    n.e.accept(this);
    System.out.print(".");
    n.i.accept(this);
    System.out.print("(");
    for (int i = 0; i < n.el.size(); i++) {
      n.el.get(i).accept(this);
      if (i + 1 < n.el.size()) {
        System.out.print(", ");
      }
    }
    System.out.print(")");
  }

  @Override
  public void visit(IntegerLiteral n) {
    System.out.print(n.i);
  }

  @Override
  public void visit(DoubleLiteral n) {
    System.out.print(n.i);
  }

  @Override
  public void visit(True n) {
    System.out.print("true");
  }

  @Override
  public void visit(False n) {
    System.out.print("false");
  }

  @Override
  public void visit(IdentifierExp n) {
    System.out.print(n.s);
  }

  @Override
  public void visit(This n) {
    System.out.print("this");
  }

  @Override
  public void visit(NewArray n) {
    System.out.print("new int[");
    n.e.accept(this);
    System.out.print("]");
  }

  @Override
  public void visit(NewObject n) {
    System.out.print("new ");
    System.out.print(n.i.s);
    System.out.print("()");
  }

  @Override
  public void visit(Not n) {
    System.out.print("!");
    n.e.accept(this);
  }

  @Override
  public void visit(Identifier n) {
    System.out.print(n.s);
  }
}
