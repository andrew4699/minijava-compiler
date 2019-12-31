class i {

  public static void main(String[] args) {
    System.out.println(new a().b());
  }
}

class a {
  int i;
  B b;
  public int b() {
    b = new B();
    return b.hello1();
  }
}

class B {
  public int hello() {
    return 1;
  }
}