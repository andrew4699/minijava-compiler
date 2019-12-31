class Booleans {

  public static void main(String[] args) {
    System.out.println(new Booleans__A().f(true, false, true, true, false));
  }
}

class Booleans__A {
  boolean x;
  boolean y;

  public int f(boolean a, boolean b, boolean c, boolean d, boolean e) {
    int res1; int res2;

    res1 = 0;
    res2 = 1;

    if (a && b) {
      System.out.println(res1);
      x = true;
    } else {
      System.out.println(res2);
      x = false;
    }

    if (b) {
      System.out.println(res1);
      if (c && d) {
        y = true;
        System.out.println(res1);
      } else {
        System.out.println(res2);
        y = false;
      }
    } else {
      System.out.println(res2);
      if (e && c) {
        System.out.println(res1);
        y = false;
      } else {
        System.out.println(res2);
        y = true;
      }
    }

    if (a && e) {
      System.out.println(res1);
      x = y && x;
    } else {
      System.out.println(res2);
      x = x && e;
    }

    if (this.g(1, 2)) {
      System.out.println(res1);
    } else {
      System.out.println(res2);
    }

    return res1 + res2;
  }

  public boolean g(int a, int b) {
    boolean res;

    if (a < b) {
      res = false;
    } else {
      res = true;
    }

    return res;
  }
}