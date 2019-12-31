package Semantics.DataTypes;

public class UnknownDataType extends DataType {

  private static UnknownDataType singleton = null;

  public static UnknownDataType getInstance() {
    if (singleton == null) {
      singleton = new UnknownDataType();
    }
    return singleton;
  }

  @Override
  public String toString() {
    return "unknown";
  }
}
