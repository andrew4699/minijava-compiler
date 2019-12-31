class IntMethodArgs {

  public static void main(String[] args) {
    System.out.println(new IntMethodArgsA().f(1, 2));
  }
}

class IntMethodArgsA {

  // no local variables, only method params
  public int f(int fx, int fy) {
    System.out.println(fx);
    System.out.println(fy);
    return fx + fy + this.g(fx, this.h(1000), 3, 4, fy + fx);
  }

  public int g(int a, int b, int c, int d, int e) {
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
    System.out.println(d);
    System.out.println(e);
    return a + b + c + d + e;
  }

  public int h(int x) {
    System.out.println(x);
    return 99;
  }
}
