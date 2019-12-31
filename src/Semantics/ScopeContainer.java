package Semantics;

import AST.Visitor.GlobalSymbolTableVisitor;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;
import Semantics.*;
import Semantics.DataTypes.*;
import java.util.Stack;

public class ScopeContainer {

  private Stack<VariableTable> scopes;

  public ScopeContainer() {
    scopes = new Stack<>();
  }

  public void push(VariableTable scope) {
    scopes.push(scope);
  }

  public void pop() {
    scopes.pop();
  }

  public DataType getType(String name) {
    GlobalSymbolTable gst = GlobalSymbolTableVisitor.getInstance().getSymbolTable();

    for (int i = scopes.size() - 1; i >= 0; i--) {
      VariableTable scope = scopes.get(i);
      DataType type = scope.get(name);

      if (type instanceof UnknownDataType) {
        continue;
      }

      if (type instanceof ObjectDataType && !gst.hasClass(type.toString())) {
        continue;
      }

      return type;
    }

    return UnknownDataType.getInstance();
  }
}
