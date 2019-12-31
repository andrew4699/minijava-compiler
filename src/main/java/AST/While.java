package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class While extends Statement {

  public Exp e;
  public Statement s;

  public While(Exp ae, Statement as, Location pos) {
    super(pos);
    e = ae;
    s = as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}

