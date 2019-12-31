package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class True extends Exp {

  public True(Location pos) {
    super(pos);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
