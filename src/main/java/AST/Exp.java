package AST;

import AST.Visitor.Visitor;
import Semantics.DataTypes.DataType;
import Semantics.DataTypes.UnknownDataType;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class Exp extends ASTNode {

  public DataType type;

  public Exp(Location pos) {
    super(pos);
    type = UnknownDataType.getInstance();
  }

  public abstract void accept(Visitor v);
}
