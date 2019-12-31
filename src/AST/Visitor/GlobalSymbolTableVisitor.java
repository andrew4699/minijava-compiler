package AST.Visitor;

import AST.*;
import Semantics.*;
import Semantics.DataTypes.*;
import java.util.*;

public class GlobalSymbolTableVisitor implements Visitor {

  private static GlobalSymbolTableVisitor singletonVisitor = null;
  private GlobalSymbolTable symbolTable;
  private VariableTable currentVariableTable;
  private String currentClassName;
  private Logger logger;
  private static String mainClass;


  private GlobalSymbolTableVisitor() {
    clearSymbolTable();
  }

  public static GlobalSymbolTableVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new GlobalSymbolTableVisitor();
    }
    return singletonVisitor;
  }

  public void clearSymbolTable() {
    logger = new Logger(GlobalSymbolTableVisitor.class);
    symbolTable = new GlobalSymbolTable();
    currentVariableTable = null;
    currentClassName = null;
    mainClass = null;
  }

  public GlobalSymbolTable getSymbolTable() {
    assert symbolTable != null;
    return symbolTable;
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
    String className = n.i1.s;

    if (symbolTable.getClassInfo(className) != null) {
      logger.err(n, "Class " + className + " already declared");
      return;
    }

    currentClassName = className;

    ClassInfo classInfo = new ClassInfo();
    String main = "main";
    MethodInfo mi = new MethodInfo(VoidDataType.getInstance());
    mi.arguments = Collections.singletonList(
        new Argument(StringArrayDataType.getInstance(), n.i2.s)
    );
    classInfo.getMethodTable().add(main, mi);

    n.s.accept(this);

    GlobalSymbolTable tmp = new GlobalSymbolTable();

    tmp.addClass(className, classInfo);

    String s = tmp.toString();

    mainClass = s.substring(s.indexOf("\n\nClass:"));

    currentClassName = null;
  }

  @Override
  public void visit(ClassDeclSimple n) {
    String className = n.i.s;

    if (symbolTable.getClassInfo(className) != null) {
      logger.err(n, "Class " + className + " already declared");
      return;
    }

    currentClassName = className;

    ClassInfo classInfo = new ClassInfo(createVariableTable(n.vl), new MethodTable());
    symbolTable.addClass(className, classInfo);

    computeMethodTable(n.ml);
  }

  private VariableTable createVariableTable(VarDeclList vl) {
    VariableTable variableTable = new VariableTable();

    for (int i = 0; i < vl.size(); i++) {
      currentVariableTable = variableTable;
      vl.get(i).accept(this);
    }

    return variableTable;
  }

  private void computeMethodTable(MethodDeclList ml) {
    for (int i = 0; i < ml.size(); i++) {
      ml.get(i).accept(this);
    }
  }

  private MethodTable getCurrentMethodTable() {
    return symbolTable.getClassMethodTable(currentClassName);
  }

  @Override
  public void visit(ClassDeclExtends n) {
    String className = n.i.s;

    if (symbolTable.getClassInfo(className) != null) {
      logger.err(n, "Class " + className + " already declared");
      return;
    }

    currentClassName = className;

    ClassInfo classInfo = new ClassInfo(n.j.s, createVariableTable(n.vl), new MethodTable());
    symbolTable.addClass(className, classInfo);

    computeMethodTable(n.ml);
  }

  @Override
  public void visit(VarDecl n) {
    String variableName = n.i.s;

    if (!(currentVariableTable.get(variableName) instanceof UnknownDataType)) {
      logger.err(n, "Variable " + variableName + " already declared");
      return;
    }

    DataType variableType = getTypeFromTypeNode(n.t);
    currentVariableTable.put(variableName, variableType);
  }

  private DataType getTypeFromTypeNode(Type t) {
    if (t instanceof IntegerType) {
      return IntegerDataType.getInstance();
    } else if (t instanceof DoubleType) {
      return DoubleDataType.getInstance();
    } else if (t instanceof BooleanType) {
      return BooleanDataType.getInstance();
    } else if (t instanceof IntArrayType) {
      return IntegerArrayDataType.getInstance();
    } else if (t instanceof IdentifierType) {
      return new ObjectDataType(((IdentifierType)t).s);
    }

    return UnknownDataType.getInstance();
  }

  @Override
  public void visit(MethodDecl n) {
    DataType returnType = getTypeFromTypeNode(n.t);
    MethodInfo methodInfo = new MethodInfo(returnType);

    for (int i = 0; i < n.fl.size(); i++) {
      Formal f = n.fl.get(i);
      DataType argumentType = getTypeFromTypeNode(f.t);
      String argumentName = f.i.s;

      if (methodInfo.getScope().get(argumentName) instanceof UnknownDataType) {
        methodInfo.addArgument(new Argument(argumentType, argumentName));
      } else {
        logger.err(f, "Variable " + argumentName + " already declared");
      }
    }

    for (int i = 0; i < n.vl.size(); i++) {
      VarDecl v = n.vl.get(i);
      DataType declType = getTypeFromTypeNode(v.t);
      String varName = v.i.s;

      if (methodInfo.getScope().get(varName) instanceof UnknownDataType) {
        methodInfo.addDeclaration(varName, declType);
      } else {
        logger.err(v, "Variable " + varName + " already declared");
      }
    }

    String methodName = n.i.s;

    if (getCurrentMethodTable().getMethodInfo(methodName) != null) {
      logger.err(n, "Method " + methodName + " already declared");
      return;
    }

    getCurrentMethodTable().add(methodName, methodInfo);
  }

  @Override
  public void visit(Formal n) {
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
  }

  @Override
  public void visit(If n) {
  }

  @Override
  public void visit(While n) {
  }

  @Override
  public void visit(Print n) {
  }

  @Override
  public void visit(Assign n) {
  }

  @Override
  public void visit(ArrayAssign n) {
  }

  @Override
  public void visit(And n) {
  }

  @Override
  public void visit(LessThan n) {
  }

  @Override
  public void visit(Plus n) {
  }

  @Override
  public void visit(Minus n) {
  }

  @Override
  public void visit(Times n) {
  }

  @Override
  public void visit(ArrayLookup n) {
  }

  @Override
  public void visit(ArrayLength n) {
  }

  @Override
  public void visit(Call n) {
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
  }

  @Override
  public void visit(NewObject n) {
  }

  @Override
  public void visit(Not n) {
  }

  @Override
  public void visit(Identifier n) {
  }

  public static class GlobalSymbolTable {

    private Map<String, ClassInfo> classes;
    private Logger logger;

    private GlobalSymbolTable() {
      classes = new HashMap<>();
      logger = new Logger(GlobalSymbolTable.class);
    }

    private void addClass(String className, ClassInfo classInfo) {
      classes.put(className, classInfo);
    }

    Set<String> getClasses() {
      return classes.keySet();
    }

    public boolean hasClass(String className) {
      return getClassInfo(className) != null;
    }

    public ClassInfo getClassInfo(String className) {
      return classes.getOrDefault(className, null);
    }

    VariableTable getGlobalScope() {
      VariableTable scope = new VariableTable();

      for (String className : classes.keySet()) {
        scope.put(className, ClassDataType.getInstance());
      }

      return scope;
    }

    MethodTable getClassMethodTable(String className) {
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return new MethodTable();
      }

      return classInfo.getMethodTable();
    }

    VariableTable getClassScope(String className) {
      if (hasInheritanceCycle(className)) {
        return new VariableTable();
      }

      Stack<VariableTable> scopes = new Stack<>();

      // Build the scopes stack upwards
      while (className != null) {
        ClassInfo classInfo = getClassInfo(className);

        if (classInfo == null) {
          return new VariableTable();
        }

        scopes.push(classInfo.getScope());
        className = classInfo.superclass;
      }

      // Merge the scopes downward so that deeper scopes override shallower scopes
      VariableTable totalInheritedScope = new VariableTable();

      while (!scopes.isEmpty()) {
        totalInheritedScope = totalInheritedScope.concat(scopes.pop());
      }

      return totalInheritedScope;
    }

    VariableTable getClassMethodScope(String className, String methodName) {
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return new VariableTable();
      }

      return classInfo.getMethodScope(methodName);
    }

    // Does not search superclasses
    private MethodInfo getMethodInfo(String className, String methodName) {
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return null;
      }

      return classInfo.getMethodInfo(methodName);
    }

    // Closest = closest to this class in the inheritance tree
    // Also searches superclasses
    DataType getClosestMethodReturnType(String className, String methodName,
        List<DataType> argumentTypes) {
      if (hasInheritanceCycle(className)) {
        return UnknownDataType.getInstance();
      }

      while (className != null) {
        ClassInfo classInfo = getClassInfo(className);

        if (classInfo == null) {
          return UnknownDataType.getInstance();
        }

        DataType returnType = classInfo.getMethodReturnType(methodName, argumentTypes);

        if (!(returnType instanceof UnknownDataType)) {
          return returnType;
        }

        className = classInfo.superclass;
      }

      return UnknownDataType.getInstance();
    }

    public boolean hasInheritanceCycle(String className) {
      return hasInheritanceCycle(className, new HashSet<String>());
    }

    public boolean hasInheritanceCycle(String className, Set<String> visited) {
      if (visited.contains(className)) {
        return true;
      }

      visited.add(className);
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo != null && classInfo.superclass != null) {
        return hasInheritanceCycle(classInfo.getSuperclass(), visited);
      }

      return false;
    }

    public void logInheritanceCycles() {
      for (String className : classes.keySet()) {
        if (hasInheritanceCycle(className)) {
          logger.err("Class " + className + " is part of an inheritance cycle");
        }
      }
    }

    public void logOverrideSignatureMismatches() {
      for (String className : classes.keySet()) {
        if (hasInheritanceCycle(className)) {
          continue;
        }

        ClassInfo classInfo = getClassInfo(className);
        MethodTable methods = classInfo.getMethodTable();

        for (Map.Entry<String, MethodInfo> method : methods.getAll()) {
          logOverrideSignatureMismatches(className, method.getKey());
        }
      }
    }

    public void logOverrideSignatureMismatches(String className, String methodName) {
      MethodInfo methodInfo = getMethodInfo(className, methodName);
      String superclass = getClassInfo(className).superclass;

      while (superclass != null) {
        MethodInfo otherMethodInfo = getMethodInfo(superclass, methodName);

        if (otherMethodInfo != null) {
          if (!otherMethodInfo.hasArgumentTypes(methodInfo.getArgumentTypes())) {
            logger.err(className + "." + methodName + " incorrectly overrides " + superclass + "."
                + methodName + " (argument types)");
          }

          if (!methodInfo.returnType.equals(otherMethodInfo.returnType)) {
            logger.err(className + "." + methodName + " incorrectly overrides " + superclass + "."
                + methodName + " (return type)");
          }
        }

        superclass = getClassInfo(superclass).superclass;
      }
    }

    @Override
    public String toString() {
      StringBuilder output = new StringBuilder();

      output.append("Global Symbol Table:");

      output.append(mainClass);

      for (Map.Entry<String, ClassInfo> entry : classes.entrySet()) {
        output.append("\n\n");

        // Class name
        output
            .append("Class: ")
            .append(entry.getKey())
            .append("\n");

        ClassInfo classInfo = entry.getValue();

        // Superclass
        output
            .append("  Superclass: ")
            .append(classInfo.getSuperclass())
            .append("\n\n");

        // Fields
        output.append("  Fields:");

        for (Map.Entry<String, DataType> field : classInfo.fields.getAll()) {
          output
              .append("\n")
              .append("    ")
              .append(field.getValue().toString())
              .append(" ")
              .append(field.getKey());
        }

        output.append("\n\n");

        // Methods
        output.append("  Methods:");

        for (Map.Entry<String, MethodInfo> method : classInfo.methods.getAll()) {
          output.append("\n");

          MethodInfo methodInfo = method.getValue();
          output
              .append("    ")
              .append(methodInfo.returnType.toString())
              .append(" ")
              .append(method.getKey())
              .append(methodInfo.toArgumentString());
        }
      }

      return output.toString();
    }
  }
}
