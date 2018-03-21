package com.bank.exceptions;

public class InvalidRoleException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidRoleException() {
    super();
  }
  
  public InvalidRoleException(String str) {
    super(str);
  }
}
