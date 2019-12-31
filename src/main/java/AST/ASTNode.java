package AST;

import java_cup.runtime.ComplexSymbolFactory.Location;

abstract public class ASTNode {

  // Line number in source file.
  public final int line_number;

  // Constructor
  public ASTNode(Location pos) {
    this.line_number = pos.getLine();
  }
}
