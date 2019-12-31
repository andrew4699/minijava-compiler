class CanDoDoublesControlFlow {

  public static void main(String[] args) {
    System.out.println(new CanDoDoublesControlFlow__class().f());
  }
}

class CanDoDoublesControlFlow__class {
  double x;
  double y;

  public int f() {
    x = 1.5;
    y = 2.3;

    if (x < y) {
      System.out.println(this.less());
    } else {
      System.out.println(this.greater());
    }

    if (y < x) {
      System.out.println(this.less());
    } else {
      System.out.println(this.greater());
    }

    while (x < y) {
      if (x < y) {
        x = x - 0.1;
      } else {
        x = y;
      }

      x = x + 0.5;
    }
    System.out.println(x);

    while (y < x) {
      x = x - 0.5;
    }
    System.out.println(x);

    return 5;
  }

  public int less() {
    System.out.println(573.5);

    if (!(x < y) && y < x) {
      System.out.println(20e4);
    } else {
      if (!(y < x) && x < y) {
        System.out.println(28e4);
      } else {
        System.out.println(32e5);
      }
    }


    return 2;
  }

  public int greater() {
    System.out.println(861.5);
    return 10;
  }
}
