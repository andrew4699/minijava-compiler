class CanDoWhileLoops {

  public static void main(String[] args) {
    System.out.println(new CanDoWhileLoops__class().f());
  }
}

class CanDoWhileLoops__class {
  public int f() {
    int x;
    x = 0;
    while (x < 10) {
      System.out.println(x);
      x = x + 1;
    }
    return 99;
  }
}