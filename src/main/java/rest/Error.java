package rest;

public class Error extends CompilerOutput {
  public String error;

  public Error(String error) {
    super(null);
    this.error = error;
  }

  public String getError() {
    return error;
  }
}
