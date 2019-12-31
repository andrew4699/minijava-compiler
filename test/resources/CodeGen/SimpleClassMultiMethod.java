class SimpleClassMultiMethod {
  public static void main(String[] args) {
    System.out.println(new SimpleClassMultiMethodA().print());
  }
}

class SimpleClassMultiMethodA {
  public int print() {
    System.out.println(this.print2());
    System.out.println(20);
    return 5;
  }

  public int print2() {
    System.out.println(10);
    return 30;
  }
}
