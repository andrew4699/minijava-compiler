class ThisAsArg {

  public static void main(String[] args) {
    System.out.println(new ThisAsArg__A().f());
  }
}

class ThisAsArg__A {
  int x;

  public int f() {
    ThisAsArg__A a;
    int res;
    System.out.println(x);
    a = new ThisAsArg__A();
    res = a.g(this);
    return this.h(new ThisAsArg__A(), this, this, new ThisAsArg__A(), this);
  }

  public int g(ThisAsArg__A a) {
    boolean res;
    res = a.set(5);
    System.out.println(x);
    return 1;
  }

  public int h(ThisAsArg__A a, ThisAsArg__A b, ThisAsArg__A c, ThisAsArg__A d, ThisAsArg__A e) {
    boolean res;

    res = a.set(1);
    System.out.println(x);
    res = b.set(2);
    System.out.println(x);
    res = c.set(3);
    System.out.println(x);
    res = d.set(4);
    System.out.println(x);
    res = e.set(5);
    System.out.println(x);

    return 1;
  }

  public boolean set(int n) {
    x = n;
    return true;
  }
}