class A {

  public static void main(String[] args) {
    System.out.println(new B().b());
  }
}

class B {
  boolean y;

  public boolean b() {
    y = true && x;
    return false;
  }
}
