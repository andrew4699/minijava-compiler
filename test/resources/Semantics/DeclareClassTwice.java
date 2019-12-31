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

class A {
  public int Gotcha(boolean b) {
    return 5;
  }
}

class A extends B {
  public int GotchaAgain(int c) {
    return 6;
  }
}
