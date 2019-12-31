class UsingThis {

  public static void main(String[] args) {
    System.out.println(new UsingThis__A().f());
  }
}

class UsingThis__A {
  int val;

  public int f() {
    UsingThis__A node;
    boolean res;

    node = this;
    res = node.set(1);
    return val;
  }

  public boolean set(int n) {
    val = n;
    return true;
  }
}
