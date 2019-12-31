class A {

  public static void main(String[] args) {
    System.out.println(new B().f());
  }
}

class B {
  int k;
  B b;

  public int f() {
    boolean x;
    x = this.g();
    return 1;
  }

  public boolean g() {
    return 0;
  }

  public B h() {
    return new D();
  }

  public D i() {
    return new B();
  }
}

class C extends B {

}

class D extends C {

}