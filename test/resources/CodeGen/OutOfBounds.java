class OutOfBounds {

  public static void main(String[] args) {
    System.out.println(new OutOfBounds__A().f());
  }
}

class OutOfBounds__A {

  public int f() {
    int[] x;
    x = new int[10];
    x[10] = 1;
    return 99;
  }
}