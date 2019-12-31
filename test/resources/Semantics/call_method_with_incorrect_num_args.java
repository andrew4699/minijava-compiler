class foo {

  public static void main(String[] args) {
    System.out.println(new bar().buzz());
  }
}

class bar {
  public int buzz() {
    return this.threeArgs(1, 2);
  }

  public int threeArgs(int i, int j, int k) {
    return i + j + k;
  }
}