class ComplexArrays {

  public static void main(String[] args) {
    System.out.println(new ComplexArrays__A().f(new int[10]));
  }
}

class ComplexArrays__A {
  int[] x;

  public int f(int[] arr) {
    int i;
    i = 0;
    x = new int[arr.length];

    while (i < arr.length) {
      x[i] = arr[i] * arr[i];
      i = i + 1;
    }

    return this.g(arr)[5];
  }

  public int[] g(int[] arr) {
    int i;
    i = 0;

    while (i < arr.length - 1) {
      x[i] = x[i] + arr[i] * arr[i];
      i = i + 1;
    }

    return x;
  }
}