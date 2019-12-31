package AST.Visitor;

import AST.*;
import CodeGen.Labels;
import Semantics.ClassInfo;
import Semantics.DataTypes.*;
import Semantics.MethodInfo;
import Semantics.VariableTable;
import java.util.*;

public class CodeGenVisitor extends GeneralVisitor {

  private static CodeGenVisitor v = null;
  private static final String MAIN_LABEL = "asm_main";

  private static final int BYTE_ALIGNMENT = 8;
  private static final int LOCAL_BYTE_ALIGNMENT = 16;

  private String currentClassName;
  private String currentMethodName;
  private StringBuffer data;
  private StringBuffer text;
  private Labels labels;
  private static final boolean debug = false;
  private int labelCount;

  // map classname to list of methods
  private Map<String, List<String>> classToLabel;

  private CodeGenVisitor() {
    data = new StringBuffer(".data\n");
    text = new StringBuffer(".text\n");
    labels = new Labels();
    labelCount = 0;
  }

  public static CodeGenVisitor getInstance() {
    if (v == null) {
      v = new CodeGenVisitor();
    }
    return v;
  }

  public static void init() {
    v = null;
  }

  @Override
  public void visit(Program n) {
    generateMethodTables();
    text.append(".globl " + MAIN_LABEL + "\n");
    super.visit(n);
  }

  @Override
  public void visit(MainClass n) {
    labelt(MAIN_LABEL);

    prologue();
    n.s.accept(this);
    epilogue();
  }

  @Override
  public void visit(ClassDeclSimple n) {
    currentClassName = n.i.s;
    visitVariablesAndMethods(n.vl, n.ml);
    currentClassName = null;
  }

  @Override
  public void visit(ClassDeclExtends n) {
    currentClassName = n.i.s;
    visitVariablesAndMethods(n.vl, n.ml);
    currentClassName = null;
  }

  @Override
  public void visit(MethodDecl n) {
    currentMethodName = n.i.s;

    labelt(labels.method(currentClassName, currentMethodName));

    MethodInfo mi = getCurrentMethod();

    prologue();

    int framesize = (mi.getNumLocalVariables() + mi.getNumParams()) * LOCAL_BYTE_ALIGNMENT;

    // pad offset until 16 byte aligned
    while (framesize % 16 != 0) {
      framesize++;
    }

    asmBinary("subq", imm(framesize), "%rsp");

    // move all params to the stack
    for (int i = 0; i < mi.arguments.size(); i++) {
      String argName = mi.arguments.get(i).name;
      String src = getMethodParamRegister(i);
      String dst = getLocalVariableRegister(argName);
      asmBinary("movsd", src, dst);
    }

    // do all the statements in the method
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
    }

    // get the return value
    n.e.accept(this);

    epilogue();

    currentMethodName = null;
  }

  @Override
  public void visit(Formal n) {
    // Do nothing, Formal just accepts type and identifier
    // Not needed for assembly code
  }

  @Override
  public void visit(Call n) {
    String className = n.e.type.toString();
    String methodName = n.i.s;

    debug("call");

    pushAll();

    // move new values into registers
    for (int i = 0; i < n.el.size(); i++) {
      Exp expr = n.el.get(i);
      expr.accept(this); // get argument

      // move rax to xmm0 as a double
      if (!(expr.type instanceof DoubleDataType)) {
        asmBinary("cvtsi2sdq", "%rax", "%xmm0");
      }

      String paramDst = getMethodParamRegister(i);
      asmBinary("movsd", "%xmm0", paramDst);
    }

    n.e.accept(this); // get "this"
    asmBinary("movq", "%rax", "%rdi");

    int methodOffset = getMethodOffset(className, methodName);
    asmBinary("movq", "0(%rdi)", "%rax");
    asmUnary("call", "*" + methodOffset + "(%rax)");

    popAll();
  }

  @Override
  public void visit(This n) {
    asmBinary("movq", "%rdi", "%rax");
  }

  @Override
  public void visit(VarDecl n) {
    // Do nothing
  }

  @Override
  public void visit(Assign n) {
    String varName = n.i.s;

    if (!isLocal(varName) && !isField(varName)) {
      throw new RuntimeException("variable " + varName + " is unknown");
    }

    if (n.e.type instanceof DoubleDataType) {
      visitd(n);
    } else {
      visiti(n);
    }
  }

  public void visiti(Assign n) {
    String varName = n.i.s;
    n.e.accept(this);

    // where is this value going?
    if (isLocal(varName)) {
      asmBinary("cvtsi2sdq", "%rax", "%xmm0");
      asmBinary("movsd", "%xmm0", getLocalVariableRegister(varName));
    } else if (isField(varName)) {
      int fieldOffset = getFieldOffset(currentClassName, varName);
      asmBinary("movq", "%rax", fieldOffset + "(%rdi)");
    }
  }

  public void visitd(Assign n) {
    String varName = n.i.s;
    n.e.accept(this);

    // where is this value going?
    if (isLocal(varName)) {
      asmBinary("movsd", "%xmm0", getLocalVariableRegister(varName));
    } else if (isField(varName)) {
      int fieldOffset = getFieldOffset(currentClassName, varName);
      asmBinary("movsd", "%xmm0", fieldOffset + "(%rdi)");
    }
  }

  @Override
  public void visit(Identifier n) {
    visitVariable(n.s);
  }

  @Override
  public void visit(IdentifierExp n) {
    visitVariable(n.s);
  }

  @Override
  public void visit(Print n) {
    debug("print");

    pushAll();
    n.e.accept(this);

    String fn;
    if (n.e.type instanceof DoubleDataType) {
      // note e puts result in xmm0 which callee function will use
      fn = "putdub";
    } else {
      asmBinary("movq", "%rax", "%rdi");
      fn = "put";
    }

    asmUnary("call", fn);

    popAll();
  }

  @Override
  public void visit(Plus n) {
    debug("plus");
    if (n.e2.type instanceof IntegerDataType) {
      visiti(n);
    } else if (n.e2.type instanceof DoubleDataType) {
      visitd(n);
    } else {
      throw new RuntimeException("Call plus on non-number type");
    }
  }

  private void visitd(Plus n) {
    pushd("%xmm1");

    n.e1.accept(this);
    asmBinary("movsd", "%xmm0", "%xmm1");
    n.e2.accept(this);
    asmBinary("addsd", "%xmm1", "%xmm0");

    popd("%xmm1");
  }

  private void visitd(Times n) {
    pushd("%xmm1");

    n.e1.accept(this);
    asmBinary("movsd", "%xmm0", "%xmm1");
    n.e2.accept(this);
    asmBinary("mulsd", "%xmm1", "%xmm0");

    popd("%xmm1");
  }

  private void visitd(Minus n) {
    pushd("%xmm1");

    n.e2.accept(this);
    asmBinary("movsd", "%xmm0", "%xmm1");
    n.e1.accept(this);
    asmBinary("subsd", "%xmm1", "%xmm0");

    popd("%xmm1");
  }

  private void visiti(Minus n) {
    push("%rdx");

    n.e2.accept(this);
    push("%rax");
    n.e1.accept(this);
    pop("%rdx");
    asmBinary("subq", "%rdx", "%rax");

    pop("%rdx");
  }

  private void visiti(Plus n) {
    push("%rdx");

    n.e1.accept(this);
    push("%rax");
    n.e2.accept(this);
    pop("%rdx");
    asmBinary("addq", "%rdx", "%rax");

    pop("%rdx");
  }

  private void visiti(Times n) {
    push("%rdx");

    n.e1.accept(this);
    push("%rax");
    n.e2.accept(this);
    pop("%rdx");
    asmBinary("imulq", "%rdx", "%rax");

    pop("%rdx");
  }

  @Override
  public void visit(Minus n) {
    debug("minus");

    if (n.e2.type instanceof IntegerDataType) {
      visiti(n);
    } else if (n.e2.type instanceof DoubleDataType) {
      visitd(n);
    } else {
      throw new RuntimeException("Call minus with non-number type");
    }
  }

  @Override
  public void visit(Times n) {
    debug("times");
    assert n.e2.type.equals(n.e1.type);
    if (n.e2.type instanceof IntegerDataType) {
      visiti(n);
    } else if (n.e2.type instanceof DoubleDataType) {
      visitd(n);
    } else {
      throw new RuntimeException("Call times with non-number type");
    }
  }

  @Override
  public void visit(IntegerLiteral n) {
    asmBinary("movq", "$" + n.i, "%rax");
  }

  private String doubleLiteral(double value) {
    labelCount++;
    String label = "double_" + labelCount;

    data.append(label);
    data.append(": .double ");
    data.append(value);
    data.append("\n");

    return label;
  }

  @Override
  public void visit(DoubleLiteral n) {
    // create the label with the next double id
    String literalLabel = doubleLiteral(n.i);
    asmBinary("movsd", literalLabel + "(%rip)", "%xmm0");
  }

  @Override
  public void visit(If n) {
    labelCount++;
    String elseLabel = "else_" + labelCount;
    String ifElseEnd = "ifelse_end_" + labelCount;

    // if (expression)
    n.e.accept(this);

    // if expression is false, skip to else
    asmBinary("cmpq", imm(1), "%rax");
    asmUnary("jne", elseLabel);

    n.s1.accept(this);

    // skip the else portion
    jumpTo(ifElseEnd);

    // else
    labelt(elseLabel);
    n.s2.accept(this);

    labelt(ifElseEnd);
  }

  // unconditional branch to label
  private void jumpTo(String label) {
    asmUnary("jmp", label);
  }

  @Override
  public void visit(While n) {
    labelCount++;
    String startLoop = "loop_" + labelCount;
    String endLoop = "end_loop_" + labelCount;

    labelt(startLoop);

    // evaluate condition into rax
    n.e.accept(this);

    // if rax == 1, then jump out of the loop
    asmBinary("cmpq", imm(1), "%rax");
    asmUnary("jne", endLoop);

    // else do the loop body
    n.s.accept(this);

    // back to the top of the loop
    jumpTo(startLoop);

    labelt(endLoop);
  }

  @Override
  public void visit(And n) {
    push("%rdx");

    labelCount++;
    String done = "end_and__" + labelCount;

    // first get the boolean values of the expressions
    n.e1.accept(this);

    // next we'll compare the two values
    asmBinary("testq", "%rax", "%rax");

    // e1 & e2 == 0, go to skip
    asmUnary("je", done);

    n.e2.accept(this);

    // we're done
    labelt(done);

    pop("%rdx");
  }

  @Override
  public void visit(LessThan n) {
    if (n.e2.type instanceof IntegerDataType) {
      visiti(n);
    } else if (n.e2.type instanceof DoubleDataType) {
      visitd(n);
    } else {
      throw new RuntimeException("Call LessThan on non-number type");
    }
  }

  private void visiti(LessThan n) {
    push("%rdx");

    labelCount++;
    String skip = "lessthan" + labelCount;
    String done = "end_lessthan_" + labelCount;

    // first get the boolean values of the expressions
    n.e1.accept(this);
    push("%rax");
    n.e2.accept(this);
    pop("%rdx");

    // next compare the two values
    asmBinary("cmpq", "%rax", "%rdx");

    // if e1 >= e2 then skip
    asmUnary("jge", skip);

    // else e1 < e2, set rax to true
    asmBinary("movq", imm(1), "%rax");
    jumpTo(done);

    // e1 >= e2, set rax to false
    labelt(skip);
    zeroRax();

    // we're done, clean up
    labelt(done);
    pop("%rdx");
  }

  private void visitd(LessThan n) {
    pushd("%xmm1");

    labelCount++;
    String skip = "lessthan" + labelCount;
    String done = "end_lessthan_" + labelCount;

    // first get the boolean values of the expressions
    n.e1.accept(this);
    pushd("%xmm0");
    n.e2.accept(this);
    popd("%xmm1");

    // next compare the two values
    asmBinary("ucomisd", "%xmm0", "%xmm1");

    // if e1 >= e2 then skip
    asmUnary("jae", skip);

    // else e1 < e2, set rax to true
    asmBinary("movq", imm(1), "%rax");
    jumpTo(done);

    // e1 >= e2, set rax to false
    labelt(skip);
    zeroRax();

    // we're done, clean up
    labelt(done);
    popd("%xmm1");
  }

  private void zeroRax() {
    asmBinary("xorq", "%rax", "%rax");
  }

  @Override
  public void visit(True n) {
    asmBinary("movq", imm(1), "%rax");
  }

  @Override
  public void visit(False n) {
    zeroRax();
  }

  @Override
  public void visit(Not n) {
    // assume rax holds a boolean after expression is visited
    n.e.accept(this);

    labelCount++;
    String convertToZero = "not_" + labelCount;
    String done = "endnot_" + labelCount;

    asmBinary("cmpq", "%rax", "%rax");
    asmUnary("jnz", convertToZero);

    // here we assume %rax == 0
    asmBinary("addq", imm(1), "%rax");
    jumpTo(done);

    // here we assume %rax != 0
    labelt(convertToZero);
    asmBinary("xorq", "%rax", "%rax"); // now %rax == 0

    labelt(done);
  }

  @Override
  public void visit(ArrayAssign n) {
    pushAll();
    push("%r15");
    push("%r14");
    push("%r13");
    push("%r12");

    /*
    rax = pointer to the array
    r14 = length of the array
    r15 = value were assigning
    rdx = index into the array
     */

    String arrIdx = "%r14"; // r14 = idx
    String arrLength = "%r15"; // r15 = array.length
    String arrAddr = "%r13";
    String rvalue = "%r12";

    // index into array into rdx
    n.e1.accept(this);
    push("%rax");

    // rvalue into rax, then into r15
    n.e2.accept(this);

    pop("%rdx"); // rdx = arr idx
    asmBinary("movq", "%rdx", arrIdx);
    asmBinary("movq", "%rax", rvalue); // set rvalue register

    // get the array into rax
    n.i.accept(this);
    asmBinary("movq", "0(%rax)", arrLength);
    asmBinary("movq", "%rax", arrAddr);

    // check index is in bounds
    asmBinary("movq", arrIdx, "%rdi"); // 1st param is idx
    asmBinary("movq", arrLength, "%rsi"); // 2nd param is arr.length
    asmUnary("call", "check_arr_idx");

    // idx is in bounds, we can safely assign the array
    String addr = String.format(
        "%d(%s,%s,%s)",
        BYTE_ALIGNMENT,
        arrAddr,
        arrIdx,
        BYTE_ALIGNMENT
    );
    asmBinary("movq", rvalue, addr);

    // restore the state of the registers
    pop("%r12");
    pop("%r13");
    pop("%r14");
    pop("%r15");
    popAll();
  }

  @Override
  public void visit(ArrayLookup n) {
    pushAll();
    push("%r15");
    push("%r14");
    push("%r13");

    String arrIdx = "%r14"; // r14 = idx
    String arrLength = "%r15"; // r15 = array.length
    String arrAddr = "%r13";

    // put array pointer in rdx
    n.e1.accept(this);
    push("%rax");

    // put index in rax
    n.e2.accept(this);
    pop("%rdx"); // rdx = &array;

    // make sure the index is in bounds
    asmBinary("movq", "%rdx", arrAddr); // r13 = &array
    asmBinary("movq", "%rax", arrIdx); // r14 = idx
    asmBinary("movq", "0(%rdx)", arrLength); // r15 = array.length
    asmBinary("movq", arrIdx, "%rdi"); // 1st param is idx
    asmBinary("movq", arrLength, "%rsi"); // 2nd param is array.length
    asmUnary("call", "check_arr_idx");

    // get the address of the array
    // we need to displace by 1 because the first element
    // of the array contains the length
    String addr = String.format(
        "%d(%s,%s,%d)",
        BYTE_ALIGNMENT,
        arrAddr,
        arrIdx,
        BYTE_ALIGNMENT
    );
    asmBinary("movq", addr, "%rax");

    pop("%r13");
    pop("%r14");
    pop("%r15");
    popAll();
  }

  @Override
  public void visit(ArrayLength n) {
    // move pointer to this array into rax
    n.e.accept(this);

    // load length of array into rax
    // see visit(NewArray) if you're confused about this instruction
    asmBinary("movq", "0(%rax)", "%rax");
  }

  @Override
  public void visit(NewObject n) {
    int objectSize = getObjectSize(n.i.s);

    debug("new_object");

    pushAll();
    push("%r15");
    asmBinary("movq", "$" + objectSize, "%rdi");
    asmUnary("call", "mjcalloc");
    asmBinary("leaq", labels.methodTable(n.i.s), "%r15");
    asmBinary("movq", "%r15", "0(%rax)");

    pop("%r15");
    popAll();
  }

  @Override
  public void visit(NewArray n) {
    // save register state
    pushAll();
    push("%r15");

    /*
    We're going to create an array 1 longer than requested to store the length
    of the array. In memory, the array A will look like this:

    [ A.length, A[0], A[1], A[2], ..., A[N] ]
     */

    // how big will the array be?
    n.e.accept(this);

    asmBinary("movq", "%rax", "%r15"); // r15 = array.length

    // make sure we're allocating a positive number of bytes
    asmBinary("movq", "%r15", "%rdi");
    asmUnary("call", "check_pos_num");

    // now we're preparing to allocate the memory for the array
    asmBinary("movq", "%r15", "%rax"); // rax = r15 = array.length
    asmBinary("addq", imm(1), "%rax"); // rax++; space for length metadata
    asmBinary("salq", imm(3), "%rax"); // rax = rax * sizeof(int)
    asmBinary("movq", "%rax", "%rdi"); // set up first method param
    asmUnary("call", "mjcalloc");

    // lets add the length of this array
    asmBinary("movq", "%r15", "0(%rax)");

    // restore registers
    pop("%r15");
    popAll();
  }

  // set up stack frame
  private void prologue() {
    push("%rbp");
    asmBinary("movq", "%rsp", "%rbp");
  }

  // resolve stack frame
  private void epilogue() {
    asmRaw("leave");
    asmRaw("ret");
  }

  private void pushAll(int n) {
    assert n > 0 && n <= 6;

    if (debug) {
      labelt(String.format("push_all__%s", UUID.randomUUID()));
    }

    for (int i = n - 2; i >= 0; i--) {
      pushd(getMethodParamRegister(i));
    }
    push("%rdi");
  }

  private void debug(String label) {
    if (debug) {
      labelt(String.format("%s__%s", label, UUID.randomUUID()));
    }
  }

  private void pushAll() {
    pushAll(6);
  }

  private void popAll() {
    popAll(6);
  }

  private void popAll(int n) {
    assert n > 0 && n <= 6;

    if (debug) {
      labelt(String.format("pop_all__%s", UUID.randomUUID()));
    }

    pop("%rdi");
    for (int i = 0; i < n - 1; i++) {
      popd(getMethodParamRegister(i));
    }
  }


  private String getLocalVariableRegister(String varName) {
    int which = gst()
        .getClassMethodScope(currentClassName, currentMethodName)
        .getIndex(varName);

    // when which == 0, then -8(%rbp)
    // when which == N, then -24(N+1)(%rbp)
    int offset = -BYTE_ALIGNMENT; // saved %rbp
    offset -= LOCAL_BYTE_ALIGNMENT * which; // variable offset
    return offset + "(%rbp)";
  }

  // methodParamIndex == 0 -> first method arg
  // methodParamIndex == 1 -> second method arg
  // and so forth
  private String getMethodParamRegister(int methodParamIndex) {
    switch (methodParamIndex) {
      case 0:
        return "%xmm1";
      case 1:
        return "%xmm2";
      case 2:
        return "%xmm3";
      case 3:
        return "%xmm4";
      case 4:
        return "%xmm5";
      default:
        throw new RuntimeException("Method exceeds max number of arguments");
    }
  }

  private boolean isField(String varName) {
    return getFields(currentClassName).contains(varName);
  }

  private boolean isLocal(String varName) {
    MethodInfo mi = getCurrentMethod();

    if (mi == null) {
      return false;
    }

    VariableTable vt = mi.getScope();

    return vt.has(varName);
  }

  private void visitVariable(String varName) {
    DataType dt = getDataType(varName);

    if (dt instanceof UnknownDataType ||
      (!isLocal(varName) && !isField(varName))) {
      throw new RuntimeException("unknown variable " + varName);
    }

    if (dt instanceof DoubleDataType) {
      visitVariableD(varName);
    } else {
      visitVariableI(varName);
    }
  }

  private void visitVariableI(String varName) {
    debug("visitVariableI_" + varName);

    if (isLocal(varName)) {
      String src = getLocalVariableRegister(varName);
      asmBinary("cvttsd2siq", src, "%rax"); // Convert from xmm register to rax
    } else if (isField(varName)) {
      int fieldOffset = getFieldOffset(currentClassName, varName);
      String src = fieldOffset + "(%rdi)";
      asmBinary("movq", src, "%rax");
    }
  }

  private void visitVariableD(String varName) {
    debug("visitVariableD_" + varName);

    if (isLocal(varName)) {
      String src = getLocalVariableRegister(varName);
      asmBinary("movsd", src, "%xmm0");
    } else if (isField(varName)) {
      int fieldOffset = getFieldOffset(currentClassName, varName);
      String src = fieldOffset + "(%rdi)";
      asmBinary("movsd", src, "%xmm0");
    }
  }

  private DataType getDataType(String varName) {
    VariableTable vt = gst().getClassScope(currentClassName);

    if (currentMethodName != null) {
      vt = vt.concat(gst().getClassMethodScope(currentClassName, currentMethodName));
    }

    DataType dt = vt.get(varName);
    assert dt != null;
    return dt;
  }

  private void generateMethodTables() {
    classToLabel = new LinkedHashMap<>();

    Set<String> classes = gst().getClasses();
    List<ClassInfo> cis = new ArrayList<>(classes.size());

    // first we're going make sure all classinfos have names
    // we'd also like to know how many parents each class has so we can go from the top down
    // also lets map classes array to a classinfo array
    // also lets initialize the classToMethods map
    for (String className : classes) {
      ClassInfo ci = gst().getClassInfo(className);
      ci.name = className;
      ci.parents = getNumParents(ci);
      cis.add(ci);
      classToLabel.put(ci.name, new ArrayList<>());
    }

    // sort from least num parents to highest num parents
    cis.sort(Comparator.comparingInt((ClassInfo a) -> a.parents));

    // now lets go through each class and add all of each class's methods
    for (ClassInfo ci : cis) {
      List<String> ms = classToLabel.get(ci.name);

      // first add all superclass methods
      if (ci.superclass != null) {
        // we know we've computed the methods for the superclass already
        ms.addAll(classToLabel.get(ci.superclass));
      }

      // next add all new methods to this class
      for (String m : getMethods(ci.name)) {
        int idx = getMethodIndex(ci.name, m);

        if (idx < 0) {
          // this class does not override this method, so use the method in the superclass
          ms.add(labels.method(ci.name, m));
        } else {
          // this class overrides this method, so replace it in the vtable
          ms.set(idx, labels.method(ci.name, m));
        }
      }
    }

    // we're ready to print the table now
    for (ClassInfo ci : cis) {
      labeld(labels.methodTable(ci.name));

      if (ci.superclass == null) {
        directive("quad 0");
      } else {
        directive("quad " + labels.methodTable(ci.superclass));
      }

      makeMethodTable(ci);
    }
  }

  private int getNumParents(ClassInfo ci) {
    int p = 0;

    while (ci.superclass != null) {
      p++;
      ci = gst().getClassInfo(ci.superclass);
    }

    return p;
  }

  private List<String> getMethods(String classname) {
    ClassInfo ci = gst().getClassInfo(classname);
    List<Map.Entry<String, MethodInfo>> mt = ci.getMethodTable().getAll();

    // map just the method names
    List<String> methods = new ArrayList<>();

    for (Map.Entry<String, MethodInfo> mi : mt) {
      methods.add(mi.getKey());
    }

    return methods;
  }

  private void makeMethodTable(ClassInfo ci) {
    for (String method : classToLabel.get(ci.name)) {
      directive("quad " + method);
    }
  }

  private int getMethodOffset(String className, String methodName) {
    int index = getMethodIndex(className, methodName);

    if (index < 0) {
      throw new RuntimeException("Unknown method");
    }

    return BYTE_ALIGNMENT + (index * BYTE_ALIGNMENT);
  }

  private int getMethodIndex(String className, String methodName) {
    String cn = className;
    int index = -1;

    // search for the correct offset
    // the method might be in a superclass so we need to account for that
    while (cn != null && index < 0) {
      String label = labels.method(cn, methodName);
      index = classToLabel.get(className).indexOf(label);
      cn = gst().getClassInfo(cn).superclass;
    }

    return index;
  }

  private int getFieldOffset(String className, String fieldName) {
    int fieldIdx = getFields(className).indexOf(fieldName);
    return BYTE_ALIGNMENT + (fieldIdx * BYTE_ALIGNMENT);
  }

  private List<String> getFields(String classname) {
    assert classname != null;

    ClassInfo ci = gst().getClassInfo(classname);

    List<String> res = new ArrayList<>();

    while (true) {
      for (Map.Entry<String, DataType> f : ci.getScope().getAll()) {
        res.add(f.getKey());
      }

      if (ci.superclass == null) {
        // we got all the fields, exit loop
        break;
      }

      ci = gst().getClassInfo(ci.superclass);
    }

    return res;
  }

  private int getObjectSize(String className) {
    return BYTE_ALIGNMENT + (getFields(className).size() * BYTE_ALIGNMENT);
  }

  private MethodInfo getCurrentMethod() {
    return gst()
        .getClassMethodTable(currentClassName)
        .getMethodInfo(currentMethodName);
  }

  // prints an immediate (constant) of val
  private String imm(int val) {
    return "$" + val;
  }

  // Helpers
  private void push(String value) {
    asmUnary("pushq", value);
  }

  private void pop(String dst) {
    asmUnary("popq", dst);
  }

  private void checkDoubleReg(String src) {
    if (!src.startsWith("%xmm")) {
      throw new IllegalArgumentException("Must use SSE register");
    }
  }

  private void pushd(String src) {
    checkDoubleReg(src);
    asmBinary("subq", imm(16), "%rsp");
    asmBinary("movsd", src, "(%rsp)");
  }

  private void popd(String dst) {
    checkDoubleReg(dst);
    asmBinary("movsd", "(%rsp)", dst);
    asmBinary("addq", imm(16), "%rsp");
  }

  // label in .text region
  private void labelt(String name) {
    text.append(name);
    text.append(":\n");
  }

  // label in .data region
  private void labeld(String name) {
    data.append(name);
    data.append(":\n");
  }

  private void directive(String dir) {
    data.append(".");
    data.append(dir);
    data.append("\n");
  }

  private String padRight(String instruction) {
    int padAmount = 8;
    return String.format("%-" + padAmount + "s", instruction + " ");
  }

  private void asmUnary(String instruction, String target) {
    StringBuffer line = new StringBuffer();
    line.append(padRight(instruction + " "));
    line.append(target);
    asmRaw(line.toString());
  }

  private void asmBinary(String instruction, String src, String dst) {
    StringBuffer line = new StringBuffer();
    line.append(padRight(instruction + " "));
    line.append(padRight(src + ","));
    line.append(dst);
    asmRaw(line.toString());
  }

  private void asmRaw(String asm) {
    text.append("  ");
    text.append(asm);
    text.append("\n");
  }

  @Override
  public String toString() {
    return data.toString() + text.toString();
  }
}
