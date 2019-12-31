import Parser.sym;
import Scanner.scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;

public class DemoScanner {

  public static void main(String[] args) {
    try {
      // create a scanner on the input file
      ComplexSymbolFactory sf = new ComplexSymbolFactory();
      Reader in = new BufferedReader(new InputStreamReader(System.in));
      scanner s = new scanner(in, sf);
      Symbol t = s.next_token();
      while (t.sym != sym.EOF) {
        // print each token that we scan
        System.out.print(s.symbolToString(t) + " ");
        t = s.next_token();
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


