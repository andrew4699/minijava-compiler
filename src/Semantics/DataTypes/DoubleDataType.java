package Semantics.DataTypes;

public class DoubleDataType extends DataType {

  private static DoubleDataType singleton = null;

  public static DoubleDataType getInstance() {
    if (singleton == null) {
      singleton = new DoubleDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "double";
  }
}
