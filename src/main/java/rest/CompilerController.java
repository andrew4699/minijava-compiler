package rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
public class CompilerController {
  @PostMapping("/compile")
  public CompilerOutput compile(@RequestBody String sourceCode) {
    System.out.println("src");
    System.out.println();
    System.out.println(sourceCode);
    System.out.println();

    CompilerOutput output;
    OutputStream compilerLog = catchSysOut();

    try {
      GlobalSymbolTableVisitor.getInstance().clearSymbolTable();
      ComputeTypesVisitor.getInstance().clear();
      ClassMethodsVisitor.init();
      CodeGenVisitor.init();
      ExtendsVisitor.init();
      ReturnTypeVisitor.init();
      VarDeclVisitor.init();

      Program program = parseSource(sourceCode);

      if(!isProgramValid(program)) {
        throw new Exception(compilerLog.toString());
      }

      output = new CompilerOutput(getASM(program));
    } catch(Exception ex) {
      output = new Error(ex.toString());
    } finally {
      restoreSysOut();
    }

    return output;
  }

  private Program parseSource(String sourceCode) throws Exception {
    ComplexSymbolFactory sf = new ComplexSymbolFactory();
    scanner sc = new scanner(new StringReader(sourceCode), sf);
    parser p = new parser(sc, sf);
    Symbol root = p.parse();
    return (Program)root.value;
  }

  private boolean isProgramValid(Program program) {
    boolean failed = false;
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
    return !failed;
  }

  // Must be called after isProgramValid
  private String getASM(Program program) {
    CodeGenVisitor cgVisitor = CodeGenVisitor.getInstance();
    program.accept(cgVisitor);
    return cgVisitor.toString();
  }

  private OutputStream catchSysOut() {
    OutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    return out;
  }

  private void restoreSysOut() {
    System.setOut(System.out);
  }
}
