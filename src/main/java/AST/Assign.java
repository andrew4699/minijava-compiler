package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Assign extends Statement {

  public Identifier i;
  public Exp e;

  public Assign(Identifier ai, Exp ae, Location pos) {
    super(pos);
    i = ai;
    e = ae;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}

