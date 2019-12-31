class Factorial{
	public static void main(String[] a){
		System.out.println(new A().ComputeFac(10));
	}
}

class A {
	int a;
	boolean b;

	public int ComputeFac(int num){
		int num_aux ;
		if (num < 1)
				num_aux = 1 ;
		else 
				num_aux = num * (this.ComputeFac(num-1)) ;
		return num_aux ;
	}
}

class B extends A {
  public int ComputeFac(int num) {
    return 5;
  }
}

class C extends B {
  public int ComputeFac(int num, boolean b) {
    return 10;
  }
}

class D extends A {
  public int ComputeFac(boolean q) {
    return 10;
  }
}
