package rest;

public class CompilerOutput {
  private final String asm;

  public CompilerOutput(String asm) {
    this.asm = asm;
  }

  public String getAsm() {
    return asm;
  }
}
