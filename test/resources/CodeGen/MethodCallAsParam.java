class MethodCallAsParam {

  public static void main(String[] args) {
    System.out.println(new MethodCallAsParam__class().f());
  }
}

class MethodCallAsParam__class {
  public int f() {
    return this.g(1, 10, this.h(1), 1000, 10000);
  }

  public int g(int a, int b, int c, int d, int e) {
    return a + b + c + d + e + 100000;
  }

  public int h(int x) {
    return x + 99;
  }
}