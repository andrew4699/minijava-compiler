package AST.Visitor;

import AST.*;

// Sample print visitor from MiniJava web site with small modifications for UW CSE.
// HP 10/11

public class PrettyPrintVisitor implements Visitor {

  private static PrettyPrintVisitor singletonVisitor = null;

  private int tabs;

  private PrettyPrintVisitor() {
    tabs = 0;
  }

  public static PrettyPrintVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new PrettyPrintVisitor();
    }
    return singletonVisitor;
  }

  private void indent() {
    for (int i = 0; i < tabs; i++) {
      System.out.print("  ");
    }
  }

  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
    n.m.accept(this);
    for (int i = 0; i < n.cl.size(); i++) {
      n.cl.get(i).accept(this);
    }
  }

  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    System.out.print("class ");
    n.i1.accept(this);
    System.out.println(" {");
    tabs++;
    indent();
    System.out.print("public static void main(String[] ");
    n.i2.accept(this);
    System.out.println(") {");
    tabs++;
    indent();
    n.s.accept(this);

    tabs--;
    System.out.println();
    indent();
    System.out.println("}");

    tabs--;
    System.out.println("}");
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    System.out.print("class ");
    n.i.accept(this);
    printVariablesAndMethods(n.vl, n.ml, n);
  }

  private void printVariablesAndMethods(VarDeclList vl, MethodDeclList ml, ClassDecl n) {
    System.out.println(" {");
    tabs++;
    for (int i = 0; i < vl.size(); i++) {
      indent();
      vl.get(i).accept(this);
      System.out.println();
    }
    for (int i = 0; i < ml.size(); i++) {
      ml.get(i).accept(this);
      System.out.println();
    }
    tabs--;
    System.out.println("}");
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) {
    System.out.print("class ");
    n.i.accept(this);
    System.out.print(" extends ");
    n.j.accept(this);
    printVariablesAndMethods(n.vl, n.ml, n);
  }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
    n.t.accept(this);
    System.out.print(" ");
    n.i.accept(this);
    System.out.print(";");
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
    indent();
    System.out.print("public ");
    n.t.accept(this);
    System.out.print(" ");
    n.i.accept(this);
    System.out.print("(");
    for (int i = 0; i < n.fl.size(); i++) {
      n.fl.get(i).accept(this);
      if (i + 1 < n.fl.size()) {
        System.out.print(", ");
      }
    }
    System.out.println(") {");
    tabs++;
    for (int i = 0; i < n.vl.size(); i++) {
      indent();
      n.vl.get(i).accept(this);
      System.out.println();
    }
    for (int i = 0; i < n.sl.size(); i++) {
      indent();
      n.sl.get(i).accept(this);
      if (i < n.sl.size()) {
        System.out.println();
      }
    }
    indent();
    System.out.print("return ");
    n.e.accept(this);
    System.out.println(";");
    tabs--;
    indent();
    System.out.print("}");
  }

  // Type t;
  // Identifier i;
  public void visit(Formal n) {
    n.t.accept(this);
    System.out.print(" ");
    n.i.accept(this);
  }

  public void visit(IntArrayType n) {
    System.out.print("int[]");
  }

  public void visit(BooleanType n) {
    System.out.print("boolean");
  }

  public void visit(IntegerType n) {
    System.out.print("int");
  }

  public void visit(DoubleType n) {
    System.out.print("double");
  }

  // String s;
  public void visit(IdentifierType n) {
    System.out.print(n.s);
  }

  // StatementList sl;
  public void visit(Block n) {
    for (int i = 0; i < n.sl.size(); i++) {
      // first iteration has indentation in buffer
      if (i > 0) {
        indent();
      }
      n.sl.get(i).accept(this);
      if (i + 1 < n.sl.size()) {
        System.out.println();
      }
    }
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    System.out.print("if (");
    n.e.accept(this);
    System.out.println(") {");

    // statement of if block
    tabs++;
    indent();
    n.s1.accept(this);
    System.out.println();
    tabs--;

    indent();
    System.out.println("} else {");

    // statement of else block
    tabs++;
    indent();
    n.s2.accept(this);
    System.out.println();
    tabs--;

    indent();
    System.out.print("}");
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    System.out.print("while (");
    n.e.accept(this);
    System.out.println(") {");

    // while block
    tabs++;
    indent();
    n.s.accept(this);
    System.out.println();
    tabs--;

    indent();
    System.out.print("}");
  }

  // Exp e;
  public void visit(Print n) {
    System.out.print("System.out.println(");
    n.e.accept(this);
    System.out.print(");");
  }

  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
    n.i.accept(this);
    System.out.print(" = ");
    n.e.accept(this);
    System.out.print(";");
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
    n.i.accept(this);
    System.out.print("[");
    n.e1.accept(this);
    System.out.print("] = ");
    n.e2.accept(this);
    System.out.print(";");
  }

  // Exp e1,e2;
  public void visit(And n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" && ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(LessThan n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" < ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(Plus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" + ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(Minus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" - ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(Times n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" * ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(ArrayLookup n) {
    n.e1.accept(this);
    System.out.print("[");
    n.e2.accept(this);
    System.out.print("]");
  }

  // Exp e;
  public void visit(ArrayLength n) {
    n.e.accept(this);
    System.out.print(".length");
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
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

  // int i;
  public void visit(IntegerLiteral n) {
    System.out.print(n.i);
  }

  public void visit(DoubleLiteral n) {
    System.out.print(n.i);
  }

  public void visit(True n) {
    System.out.print("true");
  }

  public void visit(False n) {
    System.out.print("false");
  }

  // String s;
  public void visit(IdentifierExp n) {
    System.out.print(n.s);
  }

  public void visit(This n) {
    System.out.print("this");
  }

  // Exp e;
  public void visit(NewArray n) {
    System.out.print("new int[");
    n.e.accept(this);
    System.out.print("]");
  }

  // Identifier i;
  public void visit(NewObject n) {
    System.out.print("new ");
    System.out.print(n.i.s);
    System.out.print("()");
  }

  // Exp e;
  public void visit(Not n) {
    System.out.print("!");
    n.e.accept(this);
  }

  // String s;
  public void visit(Identifier n) {
    System.out.print(n.s);
  }
}
