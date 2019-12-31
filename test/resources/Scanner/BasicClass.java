class A {
  A() {
    this.b = new B();
  }
}

class B extends A {
  B() {
    this.x = 5;
  }
}
