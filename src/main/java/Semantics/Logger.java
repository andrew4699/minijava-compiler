package Semantics;

import AST.ASTNode;

public class Logger {

  private String className;

  private static boolean error = false;

  public static boolean calledError() { return error; }

  /**
   * Usage: Logger log = new Logger(RandomVisitor.class); log.error(someNode, "Some problem
   * occurred!" // [RandomVisitor - 32]: Some problem occurred!
   **/
  public Logger(Class clazz) {
    className = clazz.getSimpleName();
  }

  public void err(String message) {
    String header = String.format("[%s]: ", className);
    printErr(header, message);
  }

  public void err(ASTNode node, String message) {
    String header = String.format("[%s - %d]: ", className, node.line_number);
    printErr(header, message);
  }

  private void printErr(String header, String message) {
    // no multiline errors
    assert !message.contains("\n");

    String output = header + message;

    // keep messages short
    assert output.length() <= 120;

    System.out.println(output);

    error = true;
  }
}
