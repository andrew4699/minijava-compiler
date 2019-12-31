class CanDoControlFlow {

  public static void main(String[] args) {
    System.out.println(new CanDoControlFlow__class().f(1, 2));
  }
}

class CanDoControlFlow__class {
  int x;
  int y;

  public int f(int a, int b) {
    int z;
    x = 1;
    y = 2;

    if (a < b) {
      x = this.g(1, 2, 3, 4, 5);
      y = y + 4;
    } else {
      x = this.g(5, 4, 3, 2, 1);
      y = 4;
    }

    z = 0;
    while (z < 10) {
      z = z + 1;
    }

    return 1000;
  }

  public int g(int a, int b, int c, int d, int e) {
    int x;
    x = 0;

    while (x < 10) {
      a = a + b;
      b = b + c;
      c = c + d;

      if (c < b) {
        c = c - d;
      } else {
        c = c + d;
      }

      d = d + e;
      e = e + a;
      x = x + 1;
    }

    a = a + this.nested();

    return a * b + c * d + e * x - this.sum(100);
  }

  public int nested() {
    int i; int j; int k;
    int result;

    result = 0;
    i = 0;
    j = 0;
    k = 0;

    while (i < 10) {
      System.out.println(i);
      while (j < 10) {
        System.out.println(j);
        while (k < 10) {
          System.out.println(k);
          i = i + 1;
          j = j + 1;
          k = k + 1;
          if (i < 5) {
            if (j < 5) {
              if (k < 5) {
                System.out.println(1);
                result = result + 1;
              } else {
                System.out.println(2);
                result = result + 2;
              }
            } else {
              System.out.println(3);
              result = result + 3;
            }
          } else {
            System.out.println(4);
            result = result + 4;
          }
        }
      }
    }

    return result;
  }

  public int sum(int x) {
    int i;
    i = 0;

    while (i < x) {
      i = i + 1;
    }

    // no braces!!!
    if (1 < 2) System.out.println(1111111);
    else System.out.println(22222222);


    return i + this.fn(5);
  }

  public int fn(int n) {
    int res;

    if (n < 1) {
      res = 1;
    } else {
      res = n * this.fn(n - 1);
    }

    return res;

  }
}
