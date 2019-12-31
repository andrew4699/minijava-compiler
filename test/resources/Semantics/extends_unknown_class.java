class Foo {
  public static void main(String[] args) {
    System.out.println(5 + 4 * 3 - 1);
  }
}

class A extends D {
  public int foo() {
    return 5;
  }
}

class B extends C {
  public int bar(Baz f, Bar b) {
    System.out.println(new A().method());
    return 5;
  }
}
