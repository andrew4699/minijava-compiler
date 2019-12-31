package Semantics.DataTypes;

public abstract class DataType {

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DataType)) {
      return false;
    }

    return hashCode() == other.hashCode();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public boolean isAssignableToType(DataType type) {
    return equals(type);
  }

  public abstract String toString();
}
