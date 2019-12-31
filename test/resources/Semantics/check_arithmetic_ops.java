class a {

  public static void main(String[] args) {
    System.out.println(1);
  }
}

class b {
  public int valid() {
    int a;
    int b;
    double d;
    double e;
    // check plus
    a = 1 + 1;
    b = a + 1;
    a = b + 1;
    d = 2.;
    e = .24 + 2e4 + d + 12.;

    // check minus
    a = 1 - 0;
    b = b - 1;
    a = a - b;
    d = d - e - 21.;

    // check times
    a = a * b;
    b = a * 23;
    a = 23 * b;
    d = .1 * 2e4 * e;

    return 1;
  }

  public int invalid() {
    int a;
    boolean b;
    double d;
    double e;
    b = false;
    d = .0;
    e = .0;

    // check plus
    a = 1 + b;
    a = b + 1;
    a = b + b;
    d = 2e4 + a;
    e = 2.4 + b;
    d = a + e + b;

    // check minus
    a = 1 - b;
    b = b - 1;
    b = b - b;
    d = 2e5 - a;
    e = 2.5 - b;
    d = a - e - b;

    // check times
    a = a * a;
    b = b * 1;
    a = 23 * b;
    d = 2e6 * a;
    e = 2.6 * b;
    d = a * e * b;

    return 0;
  }
}
