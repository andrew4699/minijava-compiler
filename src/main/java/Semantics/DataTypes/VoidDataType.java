package Semantics.DataTypes;

public class VoidDataType extends DataType {

  private static VoidDataType singleton = null;

  public static VoidDataType getInstance() {
    if (singleton == null) {
      singleton = new VoidDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "void";
  }
}
