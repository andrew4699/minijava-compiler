class Main {
  public static void main(String[] args) {
    System.out.println(new Clazz().helloWorld(1, 2, 3));
  }
}

class Clazz {
  boolean hi;
  int r;

  public int helloWorld(int a, int b, int c) {
    int k;
    int j;
    int i;

    k = 1;
    i = 2;
    j = 3;

    return k + i + j;
  }

  public boolean isNice(int a, int b) {
    hi = false;
    r = 1;
    b = 3;
    a = 2;
    return r + b + a;
  }
}