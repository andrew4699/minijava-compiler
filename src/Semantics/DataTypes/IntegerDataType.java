package Semantics.DataTypes;

public class IntegerDataType extends DataType {

  private static IntegerDataType singleton = null;

  public static IntegerDataType getInstance() {
    if (singleton == null) {
      singleton = new IntegerDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "int";
  }
}
