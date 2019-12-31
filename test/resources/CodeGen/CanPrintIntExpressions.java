class CanPrintIntExpressions {

  public static void main(String[] args) {
    System.out.println(new CanPrintIntExpressions__class().f());
  }
}

class CanPrintIntExpressions__class {
  public int f() {
    System.out.println(1 + 2 - (3 * 4) - 34 + 235);
    return this.g() * 12 - 2;
  }

  public int g() {
    System.out.println(1);
    System.out.println(2);
    System.out.println(3);
    System.out.println(this.h());
    System.out.println(3);
    System.out.println(2);
    System.out.println(1);
    return 1000;
  }

  public int h() {
    System.out.println(this.i() + this.k());
    return this.i() * this.i();
  }

  public int i() {
    System.out.println(10);
    System.out.println(11);
    System.out.println(12);
    return 10 - 9 * 12 + 35;
  }

  public int k() {
    System.out.println(20);
    System.out.println(21);
    System.out.println(22);
    return this.m() + 12 - 68 * 12 + 2;
  }

  public int m() {
    return 99;
  }
}