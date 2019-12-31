import AST.Program;
import AST.Visitor.*;
import AST.Visitor.GlobalSymbolTableVisitor.GlobalSymbolTable;
import Parser.parser;
import Parser.sym;
import Scanner.scanner;
import Semantics.Logger;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;

public class MiniJava {

  /* CLI requirements */
  private static final String USAGE =
      "ant java -cp build/classes:lib/java-cup-11b.jar MiniJava <FLAGS (optional)> <FILE_NAME>";

  /* Flags */
  private static final String FLAG_SCANNER = "-S";
  private static final String FLAG_SEMANTICS = "-T";
  private static final String FLAG_PRINT_PRETTY = "-P";
  private static final String FLAG_PRINT_AST = "-A";
  private static final String[] FLAGS = new String[]{
      FLAG_SCANNER,
      FLAG_PRINT_PRETTY,
      FLAG_PRINT_AST,
      FLAG_SEMANTICS
  };

  private ComplexSymbolFactory sf;
  private String[] flags;
  private scanner sc;
  private boolean failed;
  private String fileName;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.printf("Usage: %s\n", USAGE);
      System.exit(1);
    }

    try {
      MiniJava mj = new MiniJava(args);
      mj.run();
      System.exit(mj.getStatus());
    } catch (Exception e) {
      System.err.printf(
          "Unexpected internal compiler error: %s",
          e.toString()
      );
      e.printStackTrace();
      System.exit(1);
    }
  }

  private int getStatus() {
    return failed ? 1 : 0;
  }

  private MiniJava(String[] args) throws Exception {
    failed = false;

    // set up flags and filename
    flags = Arrays.copyOfRange(args, 0, args.length - 1);
    fileName = args[args.length - 1];
  }

  private Program parseProgram() {
    try {
      // set up tokens
      ComplexSymbolFactory sf = new ComplexSymbolFactory();
      sc = createScannerForFile(fileName, sf);

      parser p = new parser(sc, sf);
      Symbol root = p.parse();
      return (Program) root.value;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void run() throws Exception {
    if (flags.length == 0) {
      printAssembly();
      return;
    }

    for (String flag : flags) {
      switch (flag) {
        case FLAG_SCANNER:
          printTokens();
          break;
        case FLAG_PRINT_PRETTY:
          printPretty();
          break;
        case FLAG_PRINT_AST:
          printAST();
          break;
        case FLAG_SEMANTICS:
          printGST();
          break;
        default:
          String errorMessage = String.format(
              "%s is not a valid flag. Flags must be in %s",
              flag,
              Arrays.toString(FLAGS)
          );
          throw new IllegalArgumentException(errorMessage);
      }
    }
  }

  private void printAssembly() {
    Program program = parseProgram();
    printErrors(program);

    if (failed) {
      return;
    }

    CodeGenVisitor cgVisitor = CodeGenVisitor.getInstance();
    program.accept(cgVisitor);
    System.out.println(cgVisitor);
  }

  private void printErrors(Program program) {
    LinkedList<Visitor> visitors = new LinkedList<>();

    GlobalSymbolTableVisitor gstVisitor = GlobalSymbolTableVisitor.getInstance();

    visitors.add(gstVisitor);
    visitors.add(ComputeTypesVisitor.getInstance());
    visitors.add(CheckMainVisitor.getInstance());
    visitors.add(ExtendsVisitor.getInstance());
    visitors.add(ClassMethodsVisitor.getInstance());
    visitors.add(CheckAssignTypesVisitor.getInstance());
    visitors.add(ExprTypesVisitor.getInstance());
    visitors.add(VarDeclVisitor.getInstance());
    visitors.add(ReturnTypeVisitor.getInstance());

    for (Visitor v : visitors) {
      program.accept(v);
      failed = failed || Logger.calledError();
    }

    GlobalSymbolTable gst = gstVisitor.getSymbolTable();

    gst.logInheritanceCycles();
    gst.logOverrideSignatureMismatches();
    failed = failed || Logger.calledError();
  }

  private void printGST() {
    printErrors(parseProgram());

    GlobalSymbolTable gst = GlobalSymbolTableVisitor.getInstance().getSymbolTable();
    System.out.println(gst);
  }

  private void printPretty() {
    Program program = parseProgram();
    program.accept(PrettyPrintVisitor.getInstance());
  }

  private void printAST() {
    Program program = parseProgram();
    program.accept(ASTVisitor.getInstance());
  }

  private void printTokens() throws Exception {
    // set up tokens
    ComplexSymbolFactory sf = new ComplexSymbolFactory();
    sc = createScannerForFile(fileName, sf);

    for (Symbol token = sc.next_token(); token.sym != sym.EOF; token = sc.next_token()) {
      System.out.printf("%s ", sc.symbolToString(token));
    }

    failed = failed || sc.hasFailed();
  }

  private scanner createScannerForFile(String fileName, ComplexSymbolFactory sf) throws Exception {
    File file = new File(fileName);
    Reader in = new BufferedReader(new FileReader(file));
    return new scanner(in, sf);
  }
}
