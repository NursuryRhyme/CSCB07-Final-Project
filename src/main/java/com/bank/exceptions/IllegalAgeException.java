package com.bank.exceptions;

public class IllegalAgeException extends Exception {

  private static final long serialVersionUID = 1L;

  public IllegalAgeException() {
    super();
  }
  
  public IllegalAgeException(String str) {
    super(str);
  }
}
