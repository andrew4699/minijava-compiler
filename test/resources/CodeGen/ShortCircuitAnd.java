class ShortCircuitAnd {

  public static void main(String[] args) {
    System.out.println(new ShortCircuitAnd__class().f());
  }
}

class ShortCircuitAnd__class {
  boolean mybool;

  public int f() {
    int res;
    boolean bool;
    mybool = true;

    res = 0;
    bool = true;

    if (true && true && true && false) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (false && true && true && true) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (true && true && false && true && true && true) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (1 < 2 && 3 < 2 && 1 < 2 && 1 < 2) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (this.g() && true && true && true) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (true && true && true && true && this.g()) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    // bohemith
    if (true && 1 < 2 && this.g() && bool && mybool && true) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (false && this.h()) {
      res = res + 1;
    } else {
      res = res - 1;
    }

    if (res < 0) {
      System.out.println(1000); // winner
    } else {
      System.out.println(0-1); // loser
    }

    return res;
  }

  public boolean h() {
    System.out.println(999); // should not execute
    return true;
  }

  public boolean g() {
    System.out.println(100);
    return 1 < 2 && 2 < 1 && true && true;
  }
}