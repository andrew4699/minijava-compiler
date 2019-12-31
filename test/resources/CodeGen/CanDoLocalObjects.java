class CanDoLocalObjects {

  public static void main(String[] args) {
    System.out.println(new CanDoLocalObjects__A().f());
  }
}

class CanDoLocalObjects__A {
  public int f() {
    CanDoLocalObjects__A a;
    a = new CanDoLocalObjects__A();
    return a.a();
  }

  public int a() {
    return 1;
  }

}
