class M
{
  public static void main(String[] args)
  {
    System.out.println(2);
  }
}

class A
{
  int x;
  boolean b;

  public int a()
  {
    return 1;
  }

  public int b(int p)
  {
    return p;
  }

  public boolean c()
  {
    return b;
  }

  public boolean d()
  {
    return x < 100;
  }

  public int e()
  {
    int[] arr;
    arr = new int[30];
    arr[20] = 2;
    return arr[20];
  }
}