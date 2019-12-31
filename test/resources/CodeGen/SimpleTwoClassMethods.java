class SimpleTwoClassMethods {
  public static void main(String[] args) {
    System.out.println(new SimpleTwoClassMethodsA().print() * 5);
  }
}

class SimpleTwoClassMethodsA {
  public int print() {
    System.out.println(new SimpleTwoClassMethodsB().print());
    System.out.println(20);
    return 5;
  }
}

class SimpleTwoClassMethodsB {
  public int print() {
    System.out.println(50 * 3);
    return 2 + 5 * 20 + 2;
  }
}
