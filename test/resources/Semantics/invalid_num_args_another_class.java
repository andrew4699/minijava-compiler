class foo {

  public static void main(String[] args) {
    System.out.println(new bar().buzz(1, 2));
  }
}

class bar {
  bee b;
  public int buzz(int i, int j) {
    b = new bee();

    return b.one();
  }
}

class bee {
  public int one(int i) {
    return i + 1;
  }
}