package AST.Visitor;

import AST.*;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;
import Semantics.DataTypes.*;
import Semantics.*;
import java.util.ArrayList;
import java.util.List;

public class ComputeTypesVisitor implements Visitor {

  private static ComputeTypesVisitor singletonVisitor = null;

  private ScopeContainer scopes;
  private int tabs;
  private StringBuffer output;
  private String currentClassName;
  private Logger logger;

  private ComputeTypesVisitor() {
    clear();
  }

  public static ComputeTypesVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new ComputeTypesVisitor();
    }
    return singletonVisitor;
  }

  public void clear() {
    logger = new Logger(ComputeTypesVisitor.class);
    scopes = new ScopeContainer();

    VariableTable globalScope = GlobalSymbolTableVisitor
        .getInstance()
        .getSymbolTable()
        .getGlobalScope();

    scopes.push(globalScope);

    output = new StringBuffer();
    tabs = 0;
    currentClassName = null;
  }

  // prints indentation
  private void indent() {
    for (int i = 0; i < this.tabs; i++) {
      output.append("  ");
    }
  }

  // prints line number followed by a new line
  private void lineNumber(ASTNode n) {
    output.append(" (line ");
    output.append(n.line_number);
    output.append(")\n");
  }

  @Override
  public void visit(Program n) {
    output.append("Program\n");
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
    output.append("MainClass ");
    n.i1.accept(this);
    lineNumber(n.i1);

    // print main class statement
    tabs++;
    n.s.accept(this);
    output.append("\n");
    tabs--;

    tabs--;
  }

  private GlobalSymbolTable getGlobalSymbolTable() {
    return GlobalSymbolTableVisitor.getInstance().getSymbolTable();
  }

  @Override
  public void visit(ClassDeclSimple n) {
    tabs++;
    indent();

    output.append("Class ");
    n.i.accept(this);
    lineNumber(n.i);

    currentClassName = n.i.s;

    VariableTable scope = getGlobalSymbolTable().getClassScope(n.i.s);
    scopes.push(scope);
    printVariablesAndMethods(n.vl, n.ml);
    scopes.pop();

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

    output.append("Class ");
    n.i.accept(this);
    output.append(" extends ");
    n.j.accept(this);
    lineNumber(n.j);

    currentClassName = n.i.s;

    VariableTable scope = getGlobalSymbolTable().getClassScope(n.i.s);
    scopes.push(scope);
    printVariablesAndMethods(n.vl, n.ml);
    scopes.pop();

    tabs--;
  }

  @Override
  public void visit(VarDecl n) {
    indent();

    output.append("VarDecl ");
    n.t.accept(this);
    output.append(" ");
    n.i.accept(this);
    lineNumber(n);
  }

  @Override
  public void visit(MethodDecl n) {
    tabs++;
    indent();

    output.append("MethodDecl ");
    n.i.accept(this);
    lineNumber(n);

    // prints return type
    tabs++;
    indent();
    output.append("returns ");
    n.t.accept(this);
    output.append("\n");

    // prints parameter list
    if (n.fl.size() > 0) {
      printParameters(n);
    }

    VariableTable scope = getGlobalSymbolTable().getClassMethodScope(currentClassName, n.i.s);
    scopes.push(scope);

    // print variable declarations
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }

    // print each statement
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
      output.append("\n");
    }

    // print return statement
    indent();
    output.append("Return ");
    n.e.accept(this);
    lineNumber(n.e);

    scopes.pop();

    tabs--;
    tabs--;
  }

  private void printParameters(MethodDecl n) {
    indent();
    output.append("parameters:\n");
    tabs++;
    for (int i = 0; i < n.fl.size(); i++) {
      indent();
      n.fl.get(i).accept(this);
      output.append("\n");
    }
    tabs--;
  }

  @Override
  public void visit(Formal n) {
    n.t.accept(this);
    output.append(" ");
    n.i.accept(this);
  }

  @Override
  public void visit(IntArrayType n) {
    output.append("int[]");
  }

  @Override
  public void visit(BooleanType n) {
    output.append("boolean");
  }

  @Override
  public void visit(IntegerType n) {
    output.append("int");
  }

  @Override
  public void visit(DoubleType n) {
    output.append("double");
  }

  @Override
  public void visit(IdentifierType n) {
    output.append(n.s);
  }

  @Override
  public void visit(Block n) {
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);

      // don't start a new line on the last statement
      if (i + 1 < n.sl.size()) {
        output.append("\n");
      }
    }
  }

  @Override
  public void visit(If n) {
    indent();
    output.append("If ");
    n.e.accept(this);
    lineNumber(n.e);

    tabs++;
    n.s1.accept(this);
    lineNumber(n.s1);
    tabs--;

    indent();
    output.append("Else\n");
    tabs++;
    n.s2.accept(this);
    tabs--;
  }

  @Override
  public void visit(While n) {
    indent();
    output.append("While ");
    n.e.accept(this);
    lineNumber(n.e);

    tabs++;
    n.s.accept(this);
    tabs--;
  }

  @Override
  public void visit(Print n) {
    indent();
    output.append("Print");
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
    output.append(" = ");
    n.e.accept(this);
  }

  @Override
  public void visit(ArrayAssign n) {
    indent();
    n.i.accept(this);
    output.append("[");
    n.e1.accept(this);
    output.append("] = ");
    n.e2.accept(this);
  }

  @Override
  public void visit(And n) {
    n.type = BooleanDataType.getInstance();
    output.append("(");
    n.e1.accept(this);
    output.append(" && ");
    n.e2.accept(this);
    output.append(")");
    outputType(n.type);
  }

  @Override
  public void visit(LessThan n) {
    n.type = BooleanDataType.getInstance();
    output.append("(");
    n.e1.accept(this);
    output.append(" < ");
    n.e2.accept(this);
    output.append(")");
    outputType(n.type);
  }

  @Override
  public void visit(Plus n) {
    output.append("(");
    n.e1.accept(this);
    output.append(" + ");
    n.e2.accept(this);
    output.append(")");

    n.type = n.e1.type;
    outputType(n.type);
  }

  @Override
  public void visit(Minus n) {
    output.append("(");
    n.e1.accept(this);
    output.append(" - ");
    n.e2.accept(this);
    output.append(")");

    n.type = n.e1.type;
    outputType(n.type);
  }

  @Override
  public void visit(Times n) {
    output.append("(");
    n.e1.accept(this);
    output.append(" * ");
    n.e2.accept(this);
    output.append(")");

    n.type = n.e1.type;
    outputType(n.type);
  }

  @Override
  public void visit(ArrayLookup n) {
    n.type = IntegerDataType.getInstance();
    n.e1.accept(this);
    output.append("[");
    n.e2.accept(this);
    output.append("]");
    outputType(n.type);
  }

  @Override
  public void visit(ArrayLength n) {
    n.type = IntegerDataType.getInstance();
    n.e.accept(this);
    output.append(".length");
    outputType(n.type);
  }

  // PRE: n's children must already have their types computed
  private DataType getCallReturnType(Call n) {
    if (!(n.e.type instanceof ObjectDataType)) {
      return UnknownDataType.getInstance();
    }

    String className = ((ObjectDataType) n.e.type).className;

    List<DataType> argumentTypes = new ArrayList<>();

    for (int i = 0; i < n.el.size(); i++) {
      argumentTypes.add(n.el.get(i).type);
    }

    return getGlobalSymbolTable().getClosestMethodReturnType(className, n.i.s, argumentTypes);
  }

  @Override
  public void visit(Call n) {
    n.e.accept(this);

    output.append(".");
    n.i.accept(this);
    output.append("(");
    for (int i = 0; i < n.el.size(); i++) {
      n.el.get(i).accept(this);
      if (i + 1 < n.el.size()) {
        output.append(", ");
      }
    }

    output.append(")");

    n.type = getCallReturnType(n);
    outputType(n.type);
  }

  @Override
  public void visit(IntegerLiteral n) {
    n.type = IntegerDataType.getInstance();
    output.append(n.i);
    outputType(n.type);
  }

  @Override
  public void visit(DoubleLiteral n) {
    n.type = DoubleDataType.getInstance();
    output.append(n.i);
    outputType(n.type);
  }

  @Override
  public void visit(True n) {
    n.type = BooleanDataType.getInstance();
    output.append("true");
    outputType(n.type);
  }

  @Override
  public void visit(False n) {
    n.type = BooleanDataType.getInstance();
    output.append("false");
    outputType(n.type);
  }

  @Override
  public void visit(IdentifierExp n) {
    n.type = scopes.getType(n.s);
    output.append(n.s);
    outputType(n.type);
  }

  @Override
  public void visit(This n) {
    n.type = new ObjectDataType(currentClassName);
    output.append("this");
    outputType(n.type);
  }

  @Override
  public void visit(NewArray n) {
    n.type = IntegerArrayDataType.getInstance();
    output.append("new int[");
    n.e.accept(this);
    output.append("]");
    outputType(n.type);
  }

  @Override
  public void visit(NewObject n) {
    String className = n.i.s;
    GlobalSymbolTable gst = GlobalSymbolTableVisitor.getInstance().getSymbolTable();

    if (gst.hasClass(className)) {
      n.type = new ObjectDataType(className);
    } else {
      n.type = UnknownDataType.getInstance();
      logger.err(n, "Cannot create object of unknown class " + className);
    }

    output.append("new ");
    n.i.accept(this);
    output.append("()");
    outputType(n.type);
  }

  @Override
  public void visit(Not n) {
    n.type = BooleanDataType.getInstance();
    output.append("!");
    n.e.accept(this);
    outputType(n.type);
  }

  @Override
  public void visit(Identifier n) {
    n.type = scopes.getType(n.s);
    output.append(n.s);
    outputType(n.type);
  }

  private void outputType(DataType type) {
    output.append("{");
    output.append(type);
    output.append("}");
  }

  @Override
  public String toString() {
    return output.toString();
  }
}
