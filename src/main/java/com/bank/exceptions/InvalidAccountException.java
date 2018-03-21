package com.bank.exceptions;

public class InvalidAccountException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidAccountException() {
    super();
  }
  
  public InvalidAccountException(String str) {
    super(str);
  }
}
