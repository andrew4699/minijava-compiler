class Factorial{
	public static void main(String[] a){
		System.out.println(10);
	}
}

class A {
	A a;
  B b;

	public int good(){
    C c;

    a = new A();
    a = new B();

    b = new B();

    c = new C();
		return 5;
	}

  public int bad() {
    C c;

    a = new C();

    b = new A();
    b = new C();

    c = new A();
    c = new B();
    return 8;
  }
}

class B extends A {

}

class C {

}
