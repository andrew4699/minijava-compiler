import AST.Statement;
import AST.Visitor.PrettyPrintVisitor;
import Parser.parser;
import Scanner.scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;

public class DemoParser {

  public static void main(String[] args) {
    try {
      // create a scanner on the input file
      ComplexSymbolFactory sf = new ComplexSymbolFactory();
      Reader in = new BufferedReader(new InputStreamReader(System.in));
      scanner s = new scanner(in, sf);
      parser p = new parser(s, sf);
      Symbol root;
      // replace p.parse() with p.debug_parse() in next line to see trace of
      // parser shift/reduce actions during parse
      root = p.parse();
      List<Statement> program = (List<Statement>) root.value;
      for (Statement statement : program) {
        statement.accept(PrettyPrintVisitor.getInstance());
        System.out.print("\n");
      }
    } catch (Exception e) {
      // yuck: some kind of error in the compiler implementation
      // that we're not expecting (a bug!)
      System.err.println("Unexpected internal compiler error: " +
          e.toString());
      // print out a stack dump
      e.printStackTrace();
    }
  }
}
