package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ArrayAssign extends Statement {

  public Identifier i;
  public Exp e1, e2;

  public ArrayAssign(Identifier ai, Exp ae1, Exp ae2, Location pos) {
    super(pos);
    i = ai;
    e1 = ae1;
    e2 = ae2;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}

