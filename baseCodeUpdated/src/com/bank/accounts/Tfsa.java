package com.bank.accounts;

import java.math.BigDecimal;

public class Tfsa extends AccountImpl {
  // Account type name
  private final String typeName = "TFSA";
 
  /**
   * Creates a SavingsAccount object.
   * @param id account ID
   * @param name account name
   * @param balance account balance
   */
  public Tfsa(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    this.setType(this.typeName);
  }
}