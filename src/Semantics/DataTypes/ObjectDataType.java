package Semantics.DataTypes;

import Semantics.*;
import AST.Visitor.GlobalSymbolTableVisitor;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;

public class ObjectDataType extends DataType {

  public String className;

  public ObjectDataType(String className) {
    this.className = className;
  }

  @Override
  public String toString() {
    return className;
  }

  @Override
  public boolean isAssignableToType(DataType type) {
    if (!(type instanceof ObjectDataType)) {
      return false;
    }

    GlobalSymbolTable gst = GlobalSymbolTableVisitor.getInstance().getSymbolTable();
    String curClassName = className;
    ClassInfo cInfo = gst.getClassInfo(curClassName);

    while (cInfo.superclass != null) {
      if (curClassName.equals(type.toString())) {
        return true;
      }

      curClassName = cInfo.superclass;
      cInfo = gst.getClassInfo(curClassName);
    }

    return curClassName.equals(type.toString());
  }
}
