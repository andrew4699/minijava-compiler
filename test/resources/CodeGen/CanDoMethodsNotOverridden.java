class CanDoMethodsNotOverridden {

  public static void main(String[] args) {
    System.out.println(new CanDoMethodsNotOverridden__A().f());
  }
}

class CanDoMethodsNotOverridden__A {
  CanDoMethodsNotOverridden__B b;

  public int f() {
    CanDoMethodsNotOverridden__C c;
    c = new CanDoMethodsNotOverridden__C();
    b = new CanDoMethodsNotOverridden__B();
    return b.h() + b.g() + c.h() + c.g() + c.i();
  }

  public int h() {
    return 5;
  }
}

class CanDoMethodsNotOverridden__B extends CanDoMethodsNotOverridden__A {
  public int g() {
    return 1;
  }
}

class CanDoMethodsNotOverridden__C extends CanDoMethodsNotOverridden__B {
  public int i() {
    return 200;
  }
}