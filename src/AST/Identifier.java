package AST;

import AST.Visitor.Visitor;
import Semantics.DataTypes.DataType;
import Semantics.DataTypes.UnknownDataType;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Identifier extends ASTNode {

  public String s;
  public DataType type;

  public Identifier(String as, Location pos) {
    super(pos);
    s = as;
    type = UnknownDataType.getInstance();
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public String toString() {
    return s;
  }
}
