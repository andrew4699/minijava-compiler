class ComplexClassPrintIntegerExprs {
  public static void main(String[] args) {
    System.out.println(2 + new ComplexClassPrintIntegerExprsA().print() * 22);
  }
}

class ComplexClassPrintIntegerExprsA {
  public int print() {
    System.out.println(this.print2() * new ComplexClassPrintIntegerExprsB().print());
    return 5;
  }

  public int print2() {
    return 22 - 2 * 3 + new ComplexClassPrintIntegerExprsB().print2();
  }
}

class ComplexClassPrintIntegerExprsB {
  public int print() {
    System.out.println(new ComplexClassPrintIntegerExprsA().print2() * 8);
    System.out.println(2 + 4 * this.print2());
    return 511;
  }

  public int print2() {
    return 6;
  }
}
