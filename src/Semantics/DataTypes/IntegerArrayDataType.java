package Semantics.DataTypes;

public class IntegerArrayDataType extends DataType {

  private static IntegerArrayDataType singleton = null;

  public static IntegerArrayDataType getInstance() {
    if (singleton == null) {
      singleton = new IntegerArrayDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "int[]";
  }
}
