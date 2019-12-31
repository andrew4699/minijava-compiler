class InheritFields {

  public static void main(String[] args) {
    System.out.println(new InheritFields__A().f());
  }
}

class InheritFields__A {
  int x;
  int z;
  InheritFields__A f;

  public int f() {
    InheritFields__A a;
    a = new InheritFields__B();

    return a.f();
  }

  public int g() {
    return 1000;
  }
}

class InheritFields__B extends InheritFields__A {
  int y;

  public int f() {
    x = 1;
    y = 2;
    z = 3;
    f = new InheritFields__B();
    return x + y + z + f.g();
  }
}