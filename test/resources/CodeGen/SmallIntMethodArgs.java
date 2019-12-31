class SmallIntMethodArgs {

  public static void main(String[] args) {
    System.out.println(new SmallIntMethodArgsA().f(1));
  }
}

class SmallIntMethodArgsA {

  // no local variables, only method params
  public int f(int fx) {
    return this.g(770, this.h(fx));
  }

  public int g(int d, int e) {
    return e;
  }

  public int h(int x) {
    return x + 93;
  }
}
