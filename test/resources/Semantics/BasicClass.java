class Factorial{
	public static void main(String[] a){
		System.out.println(new A().ComputeFac(10, 2.));
	}
}

class A {
	int a;
	boolean b;
	double d;

	public int ComputeFac(int num, double dub){
		int num_aux ;
		double double_aux ;
		if (num < 1)
				num_aux = 1 ;
		else
				num_aux = num * (this.ComputeFac(num-1, 2e55)) ;
		return num_aux ;
	}
}
