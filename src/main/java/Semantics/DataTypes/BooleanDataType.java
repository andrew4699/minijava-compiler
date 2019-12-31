package Semantics.DataTypes;

public class BooleanDataType extends DataType {

  private static BooleanDataType singleton = null;

  public static BooleanDataType getInstance() {
    if (singleton == null) {
      singleton = new BooleanDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "boolean";
  }
}
