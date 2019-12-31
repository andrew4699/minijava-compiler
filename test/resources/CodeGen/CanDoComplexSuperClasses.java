class CanDoComplexSuperClasses {

  public static void main(String[] args) {
    System.out.println(new CanDoComplexSuperClasses__A().a());
  }
}

class CanDoComplexSuperClasses__A {
  CanDoComplexSuperClasses__A cclass;

  public int a() {
    cclass = new CanDoComplexSuperClasses__C();
    System.out.println(cclass.b());
    return 100;
  }

  public int b() {
    return 1000;
  }
}

class CanDoComplexSuperClasses__B extends CanDoComplexSuperClasses__A {
  CanDoComplexSuperClasses_D dclass;
  CanDoComplexSuperClasses__B bclass;

  public int a() {
    dclass = new CanDoComplexSuperClasses_D();
    bclass = new CanDoComplexSuperClasses__B();
    System.out.println(bclass.a());
    System.out.println(dclass.a());
    System.out.println(2);
    return 200;
  }
}

class CanDoComplexSuperClasses__C extends CanDoComplexSuperClasses__B {
  CanDoComplexSuperClasses__B bclass;
  CanDoComplexSuperClasses__A aclass;

  public int a() {
    bclass = new CanDoComplexSuperClasses__B();
    aclass = new CanDoComplexSuperClasses__A();
    System.out.println(aclass.a());
    System.out.println(bclass.a());
    return 300;
  }

  public int b() {
    return 3000;
  }
}

class CanDoComplexSuperClasses_D extends CanDoComplexSuperClasses__B {
  CanDoComplexSuperClasses__A dclass;

  public int a() {
    dclass = new CanDoComplexSuperClasses_D();
    System.out.println(dclass.b());
    return 400;
  }
}