class a {

  public static void main(String[] args) {
    System.out.println(1);
  }
}

class b {
  public boolean valid() {
    boolean x; boolean y;
    x = false; y = true;

    if (x && y) {
      System.out.println(1);
    } else {
      System.out.println(0);
    }

    while (x) {
      System.out.println(0);
    }

    // check &&
    x = true && y;
    y = x && false;
    x = true && true;

    // less than is a boolean?
    x = 1 < 2;

    // check !
    y = !!!!!!!!x;

    return true;
  }

  public boolean invalid() {
    boolean x; boolean y;
    int z;
    x = true; y = false; z = 0;

    if (1) {
      // oops
    } else {

    }

    if (z) {
      System.out.println(0);
    } else {
      System.out.println(1);
    }

    while (z) {
      System.out.println(0);
    }

    while (5) {
      System.out.println(0);
    }

    // check &&
    x = 1 && true;
    y = true && 1;
    x = 1 && 1;

    // less than expects int
    x = true < false;
    x = x < y;

    x = !1;

    return false;
  }
}