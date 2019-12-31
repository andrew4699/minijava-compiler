class CanMakeSimpleSuperClass {

  public static void main(String[] args) {
    System.out.println(new CanMakeSimpleSuperClass__A().a());
  }
}

class CanMakeSimpleSuperClass__A {
  CanMakeSimpleSuperClass__A myclass;

  public int a() {
    myclass = new CanMakeSimpleSuperClass__B();
    System.out.println(myclass.a());
    return 100;
  }
}

class CanMakeSimpleSuperClass__B extends CanMakeSimpleSuperClass__A {

  public int a() {
    System.out.println(2);
    return 200;
  }
}