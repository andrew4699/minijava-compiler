package Semantics.DataTypes;

public class StringArrayDataType extends DataType {

  private static StringArrayDataType singleton = null;

  public static StringArrayDataType getInstance() {
    if (singleton == null) {
      singleton = new StringArrayDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "String[]";
  }
}
