class CanUseLocalsWithFieldsEasy {
  public static void main(String[] args) {
    System.out.println(new CanUseLocalsWithFieldsEasy__class().f(1));
  }
}

class CanUseLocalsWithFieldsEasy__class {
  int x;

  public int f(int y) {
    x = 2;
    return x + y;
  }


}
