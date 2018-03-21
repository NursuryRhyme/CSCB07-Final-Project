package com.bank.accounts;

import java.math.BigDecimal;

public class BalanceOwingAccount extends AccountImpl {
  private final String typeName = "BALANCEOWING";
  
  /**
   * Creates a ChequingAccount object.
   * @param id the ID of the account
   * @param name the name of the account
   * @param balance the balance of the account
   */
  public BalanceOwingAccount(int id, String name, BigDecimal balance) {
    super(id, name, balance);
    this.setType(this.typeName);
  }
}
