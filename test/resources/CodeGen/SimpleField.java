class SimpleField {
  public static void main(String[] args) {
    System.out.println(new SimpleFieldA().print());
  }
}

class SimpleFieldA {
  int a;

  public int print() {
    System.out.println(a + 10);
    return 5;
  }
}
