class a {

  public static void main(String[] args) {
    System.out.println(new foo().bar());
  }
}

class foo {
  int i;
  public int bar() {
    return this.one()
        + this.two(1)
        + this.three(1, 2)
        + this.four(1, 2, 3);
  }
  public int one() { return 1; }
  public int two(int i) { return i;}
  public int three(int i, int j) { return i + j; }
  public int four(int i, int j, int k) {
    return i + j + k;
  }
}