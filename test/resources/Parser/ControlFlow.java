class MyClass {
  public static void main(String[] args) {
    y = 5;
  }
}

class NonMain {
  public int doStuff() {
    int x;
    x = 4;

    if(x < 5) {
      x = x + 1;
    } else {
      x = x * 2;
    }

    while(x < 4) {
      x = x + x;
    }

    return x + 5;
  }
}
