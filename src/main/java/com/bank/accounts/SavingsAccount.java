package com.bank.accounts;

import android.content.Context;

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
  public SavingsAccount(Context context, int id, String name, BigDecimal balance) {
    super(context, id, name, balance);
    this.setType(this.typeName);
  }
}
