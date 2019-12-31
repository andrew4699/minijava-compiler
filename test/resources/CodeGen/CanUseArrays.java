class CanUseArrays {

  public static void main(String[] args) {
    System.out.println(new CanUseArrays__class().f());
  }
}

class CanUseArrays__class {
  public int f() {
    int result;
    int[] arr;
    int i;

    i = 0;
    arr = new int[10];
    result = 0;

    while (i < arr.length) {
      arr[i] = i;
      result = result + arr[i];
      i = i + 1;
    }

    return result;
  }
}