class AccessArray {

  public static void main(String[] args) {
    System.out.println(new AccessArray__A().f());
  }
}

class AccessArray__A {
  int[] a;
  public int f() {
    a = new int[10];
    a[3] = 5;
    return a[3];
  }
}