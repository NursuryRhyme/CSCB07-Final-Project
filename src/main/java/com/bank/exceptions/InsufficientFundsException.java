package com.bank.exceptions;

public class InsufficientFundsException extends Exception {

  private static final long serialVersionUID = 1L;

  public InsufficientFundsException() {
    super();
  }
  
  public InsufficientFundsException(String str) {
    super(str);
  }
}
