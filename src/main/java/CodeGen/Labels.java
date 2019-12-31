package CodeGen;

public class Labels {


  public String methodTable(String className) {
    return className + "$$";
  }

  public String method(String className, String methodName) {
    return className + "$" + methodName;
  }

}
