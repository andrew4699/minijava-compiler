package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class DoubleLiteral extends Exp {

  public double i;

  public DoubleLiteral(double ai, Location pos) {
    super(pos);
    i = ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
