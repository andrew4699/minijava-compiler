class DoubleMethodArgs {

  public static void main(String[] args) {
    System.out.println(new DoubleMethodArgsA().f(1e4, 57, 2e2));
  }
}

class DoubleMethodArgsA {

  // no local variables, only method params
  public double f(double fx, int fy, double fz) {
    System.out.println(fy + 12);
    System.out.println(fx);
    return fx + fz;
  }
}
