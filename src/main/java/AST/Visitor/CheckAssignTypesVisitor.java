package AST.Visitor;

import AST.ArrayAssign;
import AST.Assign;
import Semantics.*;
import Semantics.DataTypes.*;

public class CheckAssignTypesVisitor extends GeneralVisitor {

  private static CheckAssignTypesVisitor singletonVisitor = null;

  private Logger logger;

  public CheckAssignTypesVisitor() {
    logger = new Logger(CheckAssignTypesVisitor.class);
  }

  public static CheckAssignTypesVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new CheckAssignTypesVisitor();
    }
    return singletonVisitor;
  }

  private boolean isSubclass(DataType parentDataType, DataType childDataType) {
    String parentClass = parentDataType.toString();
    String childClass = childDataType.toString();

    ClassInfo cInfo = gst().getClassInfo(childClass);

    while (cInfo.superclass != null) {
      if (parentClass.equals(childClass)) {
        return true;
      }

      childClass = cInfo.superclass;
      cInfo = gst().getClassInfo(childClass);
    }

    return parentClass.equals(childClass);
  }

  @Override
  public void visit(Assign n) {
    n.i.accept(this);
    n.e.accept(this);

    if (!n.e.type.isAssignableToType(n.i.type)) {
      logger.err(n, "Trying to assign " + n.e.type + " to " + n.i.type);
    }
  }

  @Override
  public void visit(ArrayAssign n) {
    n.i.accept(this);
    n.e1.accept(this);
    n.e2.accept(this);

    DataType dst = IntegerDataType.getInstance();

    if (!n.e2.type.equals(dst)) {
      logger.err(n, "Trying to assign " + n.e2.type + " to " + dst);
    }
  }
}
