package com.bank.accounts;

import java.math.BigDecimal;

public class RestrictedSavingsAccount extends AccountImpl {
  // Account type name
  private final String typeName = "RESTRICTEDSAVING";
 
  /**
   * Creates a RestrictedSavingsAccount object.
   * @param id account ID
   * @param name account name
   * @param balance account balance
   */
  public RestrictedSavingsAccount(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    this.setType(this.typeName);
  }

}
