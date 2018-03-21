package com.bank.users;

import android.content.Context;
import com.bank.accounts.Account;
import com.bank.database.android.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class Customer extends UserImpl {
  // The name of the role of this user
  private final String role = "CUSTOMER";
 
  private List<Account> accounts = new ArrayList<Account>();
  private DatabaseHelper dbHelper = new DatabaseHelper(this.getContext());
  
  /**
   * Creates a Customer object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   */
  public Customer(Context context, int id, String name, int age, String address) {
    super(context, id, name, age, address);
    this.setRoleId(role);
    findAndAddAccounts();
  }
  
  /**
   * Creates a Customer object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   * @param authenticated true if user is authenticated, false otherwise
   */
  public Customer(Context context, int id, String name, int age,
      String address, boolean authenticated) {
    super(context, id, name, age, address, authenticated);
    this.setRoleId(role);
    findAndAddAccounts();
  }
  
  public List<Account> getAccounts() {
    updateAccounts();
    return this.accounts;
  }
  
  public void addAccount(Account account) {
    this.accounts.add(account);
  }
  
  private void findAndAddAccounts() {
    List<Integer> accountIds = dbHelper.getUsersAccIds(this.getId());
    
    for (int accountId : accountIds) {
      this.addAccount(dbHelper.getAccountObject(accountId));
    }
  }
  
  private void updateAccounts() {
    List<Integer> accountIds = dbHelper.getUsersAccIds(this.getId());
    
    this.accounts.clear();
    for (int accountId : accountIds) {
      this.addAccount(dbHelper.getAccountObject(accountId));
    }
  }
}
