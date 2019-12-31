package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IntegerLiteral extends Exp {

  public int i;

  public IntegerLiteral(int ai, Location pos) {
    super(pos);
    i = ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
