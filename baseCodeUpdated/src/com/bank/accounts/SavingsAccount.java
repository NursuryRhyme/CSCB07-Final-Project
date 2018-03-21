package com.bank.accounts;

import java.math.BigDecimal;

public class SavingsAccount extends AccountImpl {
  // Account type name
  private final String typeName = "SAVING";
 
  /**
   * Creates a SavingsAccount object.
   * @param id account ID
   * @param name account name
   * @param balance account balance
   */
  public SavingsAccount(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    this.setType(this.typeName);
  }
}
