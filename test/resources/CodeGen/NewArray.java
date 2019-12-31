class NewArray {

  public static void main(String[] args) {
    System.out.println(new NewArray_A().f());
  }
}

class NewArray_A {
  int[] a;
  public int f() {
    a = new int[36];
    return a.length;
  }
}