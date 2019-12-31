class Main {
  public static void main(String[] args) {
    System.out.println(new B().kerchoo());
  }
}

class A {
  int r;

  public int foo(int a, int b, int c) {
    return 5;
  }
}

class B extends A {
  public int kerchoo() {
    hi = true;
    r = r + 10;
    hi = false;
    return 5;
  }
}
