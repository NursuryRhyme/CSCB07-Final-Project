package com.bank.accounts;

import android.content.Context;

import com.bank.database.android.DatabaseHelper;
import com.bank.generics.AccountMap;
import java.math.BigDecimal;
import java.util.List;

public abstract class AccountImpl implements Account {
  private int id;
  private String name;
  private BigDecimal balance;
  private int type;
  BigDecimal interestRate = null;
  private Context context;
  private DatabaseHelper dbHelper;

  /**
   * Creates an Account object.
   * @param id the ID of the account
   * @param name the name of the account
   * @param balance the balance of the account
   */
  public AccountImpl(Context context, int id, String name, BigDecimal balance) {
    this.dbHelper = new DatabaseHelper(context);
    this.context = context;
    this.setId(id);
    this.setName(name);
    this.setBalance(balance);
  }
  
  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  /**
   * Updates the account's name from the database and then returns it.
   * @return the account name
   */
  public String getName() {
    // Updating in case information was changed
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Updates the account's balance from the database and then returns it.
   * @return the account balance
   */
  public BigDecimal getBalance() {
    // Updating in case information was changed
    this.balance = dbHelper.getBalance(this.id);
    return this.balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }
  
  public int getType() {
    return this.type;
  }
  
  /**
   * Sets the type of the account according to the database.
   * @param typeName the name of the type
   */
  protected void setType(String typeName) {
    this.type = AccountMap.getInstance(this.context).getTypeId(typeName);
  }
  
  public void findAndSetInterestRate() {
    this.interestRate = dbHelper.getInterestRate(this.getType());
  }
  
  public BigDecimal getInterestRate() {
    return this.interestRate;
  }
  
  /**
   * Adds the interest set by the account type.
   */
  public void addInterest() {

    // Calculating the new balance
    BigDecimal newInterest = this.getBalance().multiply(this.interestRate)
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    BigDecimal newBalance = this.getBalance().add(newInterest);

    // Rounding
    newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
    this.setBalance(newBalance);

    // Updating the balance in the database
    dbHelper.updateAccountBalance(newBalance, this.getId());

    // get account type and name
    String typeName = dbHelper.getAccountTypeName(this.type);

    // create message
    String message = "$" + newInterest + " worth of interest has been added to your " + typeName
        + " account ID " + this.id;

    // get the user of the account
    List<Integer> users = dbHelper.getUserIds();
    for (int userId : users) {
      if (dbHelper.getUsersAccIds(userId).contains(this.id)) {
        // leave message for the user
        dbHelper.insertMessage(userId, message);
      }
    }
  }

}
