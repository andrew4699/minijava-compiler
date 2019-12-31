class PrintOverAndOver {

  public static void main(String[] args) {
    System.out.println(new PrintOverAndOver_ClassA().f());
  }
}

class PrintOverAndOver_ClassA {
  public int f() {
    System.out.println(1);
    System.out.println(1 + 1);
    System.out.println(2 * 2);
    System.out.println(3 - 1);
    System.out.println(99);
    System.out.println(99 + 11 + 23 - 23 * 53 + 35);
    System.out.println(1000 + 12 - (2 + 4) * 4);
    return 1;
  }
}