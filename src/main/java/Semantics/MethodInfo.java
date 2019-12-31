package Semantics;

import Semantics.DataTypes.DataType;
import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

  public DataType returnType;
  public List<Argument> arguments;
  public VariableTable declarations;

  public MethodInfo(DataType returnType) {
    this.returnType = returnType;
    arguments = new ArrayList<>();
    declarations = new VariableTable();
  }

  public void addArgument(Argument argument) {
    arguments.add(argument);
  }

  public void addDeclaration(String name, DataType type) {
    declarations.put(name, type);
  }

  public List<DataType> getArgumentTypes() {
    List<DataType> argumentTypes = new ArrayList<>();

    for (Argument arg : arguments) {
      argumentTypes.add(arg.type);
    }

    return argumentTypes;
  }

  public boolean hasArgumentTypes(List<DataType> argumentTypes) {
    if (argumentTypes.size() != arguments.size()) {
      return false;
    }

    for (int i = 0; i < argumentTypes.size(); i++) {
      if (!argumentTypes.get(i).isAssignableToType(arguments.get(i).type)) {
        return false;
      }
    }

    return true;
  }

  public int getNumParams() {
    assert arguments != null;
    return arguments.size();
  }

  public int getNumLocalVariables() {
    return declarations.size();
  }

  public VariableTable getScope() {
    VariableTable argumentScope = new VariableTable();

    for (Argument arg : arguments) {
      argumentScope.put(arg.name, arg.type);
    }

    return argumentScope.concat(declarations);
  }

  public String toArgumentString() {
    List<String> parts = new ArrayList<>();

    for (Argument argument : arguments) {
      parts.add(argument.type.toString() + " " + argument.name);
    }

    return "(" + String.join(", ", parts) + ")";
  }
}
