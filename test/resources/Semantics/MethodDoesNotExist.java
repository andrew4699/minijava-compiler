class T {

  public static void main(String[] args) {
    System.out.println(new S().cool());
  }
}

class S {
  int i;
  public int cool() {
    return this.doesntExist();
  }
}