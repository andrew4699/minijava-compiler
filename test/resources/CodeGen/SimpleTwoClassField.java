class SimpleTwoClassField {
  public static void main(String[] args) {
    System.out.println(new SimpleTwoClassFieldA().print());
  }
}

class SimpleTwoClassFieldA {
  int a;

  public int print() {
    System.out.println(a + 10 * new SimpleTwoClassFieldB().print());
    return 5;
  }
}

class SimpleTwoClassFieldB {
  int a;
  int b;

  public int print() {
    System.out.println(a + 3 * b);
    return 15;
  }
}
