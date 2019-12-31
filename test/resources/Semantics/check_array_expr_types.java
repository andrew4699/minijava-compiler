class a {

  public static void main(String[] args) {
    System.out.println(1);
  }
}

class b {
  public boolean valid() {
    int[] x; int b;
    b = 10;
    x = new int[b];
    x[1] = 0;
    x[b - 5] = 1;
    x[4] = x[b - 5];
    x[x.length - 1] = x.length;

    return true;
  }

  public boolean invalid() {
    int[] x; int b;

    x = new int[false];
    x[x] = 2;
    x[1] = true;
    b = 0;
    x[2] = b.length;

    return false;
  }
}