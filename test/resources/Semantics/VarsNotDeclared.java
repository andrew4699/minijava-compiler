class MyClass {
  public static void main(String[] args) {
    System.out.println(new YourClass().yourFunc());
  }
}

class YourClass {
  int a;
  int b;

  public int yourFunc() {
    a = 100;
    b = 12;
    c = 20;
    return new YourClass().yourFunc();
  }
}