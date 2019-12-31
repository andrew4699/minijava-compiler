package Semantics.DataTypes;

public class ClassDataType extends DataType {

  private static ClassDataType singleton = null;

  public static ClassDataType getInstance() {
    if (singleton == null) {
      singleton = new ClassDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "class";
  }
}
