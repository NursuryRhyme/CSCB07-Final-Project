package com.bank.exceptions;

public class InsuffiecintFundsException extends Exception {

  private static final long serialVersionUID = 1L;

  public InsuffiecintFundsException() {
    super();
  }
  
  public InsuffiecintFundsException(String str) {
    super(str);
  }
}
