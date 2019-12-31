class CanUseLocalVars {
    public static void main(String[] args) {
        System.out.println(new CanUseLocalVars__class().f());
    }
}

class CanUseLocalVars__class {
    public int f() {
        int x;
        int y;
        y = 2;
        x = 1;
        return y + x * this.g(1, this.sum(1, 2, this.g(99, 88, 77), 4, 5), 3);
    }

    public int g(int x, int y, int z) {
      int a;
      a = 1000;
      System.out.println(x);
      System.out.println(y);
      System.out.println(z);
      return x + y + z - a;
    }

    public int sum(int a, int b, int c, int d, int e) {
      int x; int y; int z;
      x = a + b;
      y = d + e - c;
      z = b * c - e;
      System.out.println(x);
      System.out.println(y);
      System.out.println(z);
      return this.g(x, z, y);
    }
}
