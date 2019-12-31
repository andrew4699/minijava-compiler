package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IdentifierExp extends Exp {

  public String s;

  public IdentifierExp(String as, Location pos) {
    super(pos);
    s = as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
