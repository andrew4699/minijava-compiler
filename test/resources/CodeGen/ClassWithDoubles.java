class ClassWithDoubles {

  public static void main(String[] args) {
    System.out.println(new ClassWithDoubles__class().fn());
  }
}

class ClassWithDoubles__class {
  double x;
  double y;

  public double fn() {
    x = 1.0;
    y = 2.0;
    return x + y;
  }
}