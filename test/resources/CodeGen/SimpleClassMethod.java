class SimpleClassMethod {
  public static void main(String[] args) {
    System.out.println(new SimpleClassMethodA().print());
  }
}

class SimpleClassMethodA {
  public int print() {
    System.out.println(20);
    return 5;
  }
}
