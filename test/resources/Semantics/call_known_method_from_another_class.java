class hi {

  public static void main(String[] args) {
    System.out.println(new ho().hehe());
  }
}

class ho {
  ha x;

  public int hehe() {
    boolean y;
    x = new ha();
    y = x.haha(true);
    return 1;
  }
}

class ha {
  public boolean haha(boolean t) {
    return !t;
  }
}