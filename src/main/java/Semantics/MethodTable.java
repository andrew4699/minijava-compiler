package Semantics;

import Semantics.DataTypes.DataType;
import Semantics.DataTypes.UnknownDataType;
import java.util.*;

public class MethodTable {

  InsertionOrderedHashMap<String, MethodInfo> methods;

  public MethodTable() {
    methods = new InsertionOrderedHashMap<>();
  }

  public void add(String name, MethodInfo methodInfo) {
    methods.put(name, methodInfo);
  }

  public MethodInfo getMethodInfo(String name) {
    if (name == null || !methods.containsKey(name)) {
      return null;
    }

    return methods.get(name);
  }

  public VariableTable getScope(String methodName) {
    MethodInfo methodInfo = getMethodInfo(methodName);

    if (methodInfo == null) {
      return new VariableTable();
    }

    return methodInfo.getScope();
  }

  public DataType getReturnType(String methodName, List<DataType> argumentTypes) {
    if (!methods.containsKey(methodName)) {
      return UnknownDataType.getInstance();
    }

    MethodInfo method = getMethodInfo(methodName);

    if (method != null && method.hasArgumentTypes(argumentTypes)) {
      return method.returnType;
    }

    return UnknownDataType.getInstance();
  }

  public int getIndex(String methodName) {
    return methods.getInsertionOrder(methodName);
  }

  public boolean has(String methodName) {
    return methods.containsKey(methodName);
  }

  public List<Map.Entry<String, MethodInfo>> getAll() {
    return methods.entryList();
  }
}
