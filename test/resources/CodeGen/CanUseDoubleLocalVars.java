class CanUseDoubleLocalVars {
    public static void main(String[] args) {
        System.out.println(new CanUseDoubleLocalVars__class().f());
    }
}

class CanUseDoubleLocalVars__class {
    public double f() {
        double x;
        double y;
        y = 2.5;
        x = 1.2;
        return y + x * this.g(1., this.sum(1e3, this.g(99.5, 88e2, 77), 2, 4, 5), 3);
    }

    public double g(double x, double y, int z) {
      double a;
      a = 1000.;
      System.out.println(x);
      System.out.println(y);
      System.out.println(z);
      return a - x * new CanUseDoubleLocalVars__classB().put_it_to_the_test();
    }

    public double sum(double a, double b, int c, int d, int e) {
      double x; int y; int z;
      x = a + b;
      y = d + e - c;
      z = d * c - e;
      System.out.println(x);
      System.out.println(y);
      System.out.println(z);
      return this.g(x, 1e4, y);
    }

    public int simple() {
      return 5;
    }
}

class CanUseDoubleLocalVars__classB {
  int[] vIntArr;
  CanUseDoubleLocalVars__classB self;
  double vDouble;

  public double put_it_to_the_test() {
    int vInt;
    boolean vBool;
    vInt = 88;
    self = this;
    vDouble = 5e2;
    vIntArr = new int[20];
    vBool = true;
    vIntArr[3] = 02;

    return this.many(vInt, vDouble, self, vIntArr, vBool);
  }

  public double many(int i, double d, CanUseDoubleLocalVars__classB c, int[] a, boolean b) {
    System.out.println(i + 1);
    System.out.println(d + 2.0);
    System.out.println(c.other_class());
    System.out.println(a[3]);

    if (b) {
      System.out.println(3);
    } else {
      System.out.println(6);
    }

    if (!b) {
      System.out.println(3 * a.length);
    } else {
      System.out.println(6 * a.length);
    }

    return 10.5;
  }

  public double other_class() {
    CanUseDoubleLocalVars__class other;
    other = new CanUseDoubleLocalVars__class();
    System.out.println(other.simple());
    return 2.;
  }
}
