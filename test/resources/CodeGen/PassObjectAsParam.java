class PassObjectAsParam {

  public static void main(String[] args) {
    System.out.println(new PassObjectAsParam__A().f(new PassObjectAsParam__B()));
  }
}

class PassObjectAsParam__A {
  public int f(PassObjectAsParam__A a) {
    return a.g();
  }

  public int g() {
    return 1;
  }
}

class PassObjectAsParam__B extends PassObjectAsParam__A {
  public int g() {
    return 2;
  }
}