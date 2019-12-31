package Semantics;

import Semantics.DataTypes.DataType;

public class Argument {

  public DataType type;
  public String name;

  public Argument(DataType type, String name) {
    this.type = type;
    this.name = name;
  }
}
