package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MainClass extends ASTNode {

  public Identifier i1, i2, mid, at;
  public Statement s;

  public MainClass(Identifier ai1, Identifier ai2, Statement as,
      Identifier mainid, Identifier argtype, Location pos) {
    super(pos);
    i1 = ai1;
    i2 = ai2;
    s = as;
    mid = mainid; // Identifier for the main method
    at = argtype; // Identifier for the first argument type (without the [])
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
