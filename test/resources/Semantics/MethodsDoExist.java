class m {

  public static void main(String[] args) {
    System.out.println(new hello().hi());
  }
}

class hello {
  public int x() {
    return this.hi();
  }

  public int hi() {
    return 1 + 1;
  }

  public int hellothere(int a, int b) {
    return this.hi() + 1;
  }
}