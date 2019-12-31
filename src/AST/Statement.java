package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class Statement extends ASTNode {

  public Statement(Location pos) {
    super(pos);
  }

  public abstract void accept(Visitor v);
}
