package com.bank.exceptions;

public class InsufficientPermissionException extends Exception {

  private static final long serialVersionUID = 1L;

  public InsufficientPermissionException() {
    super();
  }
  
  public InsufficientPermissionException(String str) {
    super(str);
  }
}
