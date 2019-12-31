class CanUseLocalsWithFields {
  public static void main(String[] args) {
    System.out.println(new CanUseLocalsWithFields__class().f(1));
  }
}

class CanUseLocalsWithFields__class {
  int x;
  int y;

  public int f(int x) {
    int g;
    g = 1000;
    return g * this.g(x, y);
  }

  // double shadoe :-O
  public int g(int x, int y) {
    return this.h(y, x);
  }

  // combine local method params with fields
  public int h(int a, int b) {
    y = 2;
    return this.i(a + x, b * y);
  }

  // combine all locals with fields
  public int i(int a, int b) {
    int x;
    int f;
    x = 200;
    f = 1000;
    return y + x + f + a + b;
  }


}