class CanUseDoubleLocalsWithFields {
  public static void main(String[] args) {
    System.out.println(new CanUseDoubleLocalsWithFields__class().f(1e6));
  }
}

class CanUseDoubleLocalsWithFields__class {
  double x;
  double y;

  public double f(double x) {
    double g;
    g = 1000.;
    return g * this.g(x, false, y) * this.g(x, true, y);
  }

  // double shadoe :-O
  public double g(double x, boolean z, double y) {
    double q;

    if (z) {
      q = 5e2;
    } else {
      q = 5.3;
    }

    return this.h(y, x) + q;
  }

  // combine local method params with fields
  public double h(double a, double b) {
    y = 2e6;
    return this.i(a + x, b * y);
  }

  // combine all locals with fields
  public double i(double a, double b) {
    double x;
    double f;
    x = 200e2;
    f = 1000e2;
    return y + x + f + a + b;
  }


}
