package com.bank.accounts;

import java.math.BigDecimal;

public class ChequingAccount extends AccountImpl {
  // Account type name
  private final String typeName = "CHEQUING";
  
  /**
   * Creates a ChequingAccount object.
   * @param id the ID of the account
   * @param name the name of the account
   * @param balance the balance of the account
   */
  public ChequingAccount(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    this.setType(this.typeName);
  }
}
