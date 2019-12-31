class CanUseIfElse {

  public static void main(String[] args) {
    System.out.println(new CanUseIfElse__class().f());
  }
}

class CanUseIfElse__class {
  public int f() {
    if (true) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }

    if (false) {
      System.out.println(3);
    } else {
      System.out.println(4);
    }

    if (1 < 2) {
      System.out.println(5);
    } else {
      System.out.println(6);
    }

    if (2 < 1) {
      System.out.println(7);
    } else {
      System.out.println(8);
    }

    if (true && false) {
      System.out.println(9);
    } else {
      System.out.println(10);
    }

    if (true && true) {
      System.out.println(11);
    } else {
      System.out.println(12);
    }

    if (!false) {
      System.out.println(13);
    } else {
      System.out.println(14);
    }

    if (!false && 1 < 3) {
      System.out.println(15);
    } else {
      System.out.println(16);
    }

    return 1;
  }
}