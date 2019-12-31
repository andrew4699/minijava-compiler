package AST.Visitor;

import AST.MainClass;
import Semantics.Logger;

public class CheckMainVisitor extends GeneralVisitor {

  private static CheckMainVisitor singletonVisitor = null;

  private Logger logger;

  public CheckMainVisitor() {
    logger = new Logger(CheckMainVisitor.class);
  }

  public static CheckMainVisitor getInstance() {
    if (singletonVisitor == null) {
      singletonVisitor = new CheckMainVisitor();
    }
    return singletonVisitor;
  }

  @Override
  public void visit(MainClass n) {
    n.i1.accept(this);
    n.s.accept(this);

    if (!n.mid.s.equals("main")) {
      logger.err(n, "Main method must have name 'main'");
    }

    if (!n.at.s.equals("String")) {
      logger.err(n, "Main method must have argument type 'String[]'");
    }
  }
}
