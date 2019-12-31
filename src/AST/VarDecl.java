package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class VarDecl extends ASTNode {

  public Type t;
  public Identifier i;

  public VarDecl(Type at, Identifier ai, Location pos) {
    super(pos);
    t = at;
    i = ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
