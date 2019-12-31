class AssignBoolean {

  public static void main(String[] args) {
    System.out.println(new AssignBoolean__A().f());
  }
}

class AssignBoolean__A {
  boolean x;

  public int f() {
    x = true && true;

    if (x) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }

    return 99;
  }
}