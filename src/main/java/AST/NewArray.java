package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class NewArray extends Exp {

  public Exp e;

  public NewArray(Exp ae, Location pos) {
    super(pos);
    e = ae;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
