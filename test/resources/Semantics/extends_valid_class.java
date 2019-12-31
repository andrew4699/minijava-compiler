class Foo {
  public static void main(String[] args) {
    System.out.println(5 + 4 * 3 - 1);
  }
}

class A {
  public int foo() {
    return 5;
  }
}

class B extends A {
  public int bar(Baz f, Bar b) {
    System.out.println(new C().method());
    return 5;
  }
}

class C extends A {
  public int foo() {
    return 5;
  }
}

class D extends B {
  public int foo() {
    return 5;
  }
}
