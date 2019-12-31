class Factorial{
	public static void main(String[] a){
		System.out.println(new A().ComputeFac(10));
	}
}

class A {
	int a;
	boolean b;
  int b;

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
  boolean a;
  int b;

  public int ComputeFac(int num2) {
    return num2;
  }
}

class C extends B {
  int a;
  boolean b;
  int a;
  int b;
  int b;

  public int ComputeFac(int num3) {
    int a;
    boolean a;
    int a;
    return 10;
  }

  public boolean Kerchoo(boolean a, int b, boolean a, int b) {
    int a;
    return true;
  }
}
