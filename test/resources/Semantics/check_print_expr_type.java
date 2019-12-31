class a {

  public static void main(String[] args) {
    System.out.println(1);
  }
}

class b {
  public boolean valid() {
    int x; int[] y;
    y = new int[3];
    y[2] = 1;
    x = 1;
    System.out.println(x);
    System.out.println(x + 1 * 2 - 23);
    System.out.println(0);
    System.out.println(y[2]);
    System.out.println(1.0);
    System.out.println(1.0 * 2e20 - .5);

    return true;
  }

  public boolean invalid() {
    boolean x; int[] y;
    y = new int[2];
    x = false;
    System.out.println(false);
    System.out.println(x);
    System.out.println(y);

    System.out.println(11e55 * 1);
    System.out.println(1.0 + 1);
    System.out.println(1.0 - 1);

    return false;
  }
}
