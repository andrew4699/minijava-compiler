package Semantics;

import Semantics.DataTypes.DataType;
import java.util.List;

public class ClassInfo {

  public String superclass;
  public VariableTable fields;
  public MethodTable methods;
  public String name;
  public int parents;

  public ClassInfo(VariableTable fields, MethodTable methods) {
    this(null, fields, methods);
  }

  public ClassInfo() {
    superclass = null;
    fields = new VariableTable();
    methods = new MethodTable();
  }

  public ClassInfo(String superclass, VariableTable fields, MethodTable methods) {
    this.superclass = superclass;
    this.fields = fields;
    this.methods = methods;
  }

  public DataType getMethodReturnType(String methodName, List<DataType> argumentTypes) {
    return methods.getReturnType(methodName, argumentTypes);
  }

  public MethodInfo getMethodInfo(String methodName) {
    return methods.getMethodInfo(methodName);
  }

  public VariableTable getScope() {
    return fields;
  }

  public MethodTable getMethodTable() {
    return methods;
  }

  public VariableTable getMethodScope(String methodName) {
    return methods.getScope(methodName);
  }

  public String getSuperclass() {
    if (superclass == null) {
      return "None";
    }

    return superclass;
  }
}
