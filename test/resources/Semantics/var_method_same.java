class a {

  public static void main(String[] args) {
    System.out.println(new b().valid());
  }
}
class b {
  int valid;
  public int valid() {
    valid = 1;
    return valid;
  }
}