package Semantics;

import Semantics.DataTypes.DataType;
import Semantics.DataTypes.UnknownDataType;
import java.util.Map;
import java.util.List;

public class VariableTable {

  InsertionOrderedHashMap<String, DataType> variables;

  public VariableTable() {
    variables = new InsertionOrderedHashMap<>();
  }

  public void put(String name, DataType type) {
    variables.put(name, type);
  }

  public DataType get(String name) {
    if (name == null || !variables.containsKey(name)) {
      return UnknownDataType.getInstance();
    }

    return variables.get(name);
  }

  public int size() {
    return variables.size();
  }

  public int getIndex(String variableName) {
    return variables.getInsertionOrder(variableName);
  }

  public VariableTable concat(VariableTable other) {
    VariableTable newTable = new VariableTable();

    for (Map.Entry<String, DataType> entry : getAll()) {
      newTable.put(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, DataType> entry : other.getAll()) {
      newTable.put(entry.getKey(), entry.getValue());
    }

    return newTable;
  }

  public boolean has(String name) {
    return !(get(name) instanceof UnknownDataType);
  }

  public List<Map.Entry<String, DataType>> getAll() {
    return variables.entryList();
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();

    for (Map.Entry<String, DataType> entry : getAll()) {
      String nextEntry = String.format("%s -> %s\n", entry.getKey(), entry.getValue());
      output.append(nextEntry);
    }

    return output.toString();
  }
}
