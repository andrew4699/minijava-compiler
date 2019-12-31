class foo {

  public static void main(String[] args) {
    System.out.println(new B().choochoo(33));
  }
}

class A {
  public int foo(int q) {
    return q - 5;
  }
}

class B extends A {
  public int choochoo(int a) {
    return this.foo(a + 3);
  }
}
