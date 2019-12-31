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

  public int ComputeFac(int num1point5) {
    return 2;
  }
}

class B extends A {
  public int ComputeFac(int num2) {
    return num2;
  }
}

class C extends B {
  public int ComputeFac(int num3) {
    return 10;
  }

  public int ComputeFac(int num4) {
    return 15;
  }
}
