class foo {

  public static void main(String[] args) {
    System.out.println(new bar().buzz());
  }
}

class bar extends foo {
  public int buzz() {
    return this.one()
        + this.two(2)
        + this.three(3)
        + this.oops();
  }
  public int one() {
    return 1;
  }
  public int two(int t) {
    return t;
  }
  public int three(int t) {
    return t;
  }
}