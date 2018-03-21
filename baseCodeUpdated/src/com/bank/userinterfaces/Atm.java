package com.bank.userinterfaces;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.InsufficientPermissionException;
import com.bank.exceptions.InsuffiecintFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.generics.AccountMap;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.users.Customer;
import com.bank.users.User;
import java.math.BigDecimal;
import java.util.List;

public class Atm {
  private Customer currentCustomer = null;
  private boolean authenticated = false;
  
  /**
   * Creates an ATM object.
   * @param customerId the ID of a user in the database.
   * @param password the password for the user
   */
  public Atm(int customerId, String password) {
    User user = DatabaseSelectHelper.getUserDetails(customerId);
    
    // Checking if the customer is a Customer object
    if (user.getRoleId() == RoleMap.getInstance().getRoleId("CUSTOMER")) {
      this.currentCustomer = (Customer)user;
      
      if (this.currentCustomer != null) {
        this.authenticated = currentCustomer.authenticated(password);
      }
    }
  }
  
  /**
   * Creates an ATM object.
   * @param customerId the ID of a user in the database.
   */
  public Atm(int customerId) {
    User user = DatabaseSelectHelper.getUserDetails(customerId);
    
    // Checking if the customer is a Customer object
    if (user.getRoleId() == RoleMap.getInstance().getRoleId("CUSTOMER")) {
      this.currentCustomer = (Customer)user;
    }
  }
  
  /**
   * Authenticates the current user.
   * @param userId the ID of the user
   * @param password the input password
   * @return true if the user was authenticated, false otherwise
   */
  public boolean authenticate(int userId, String password) {
    boolean completed = false;
    
    if (this.currentCustomer != null) {
      completed = this.currentCustomer.authenticated(password);
      this.authenticated = completed;
    }
    
    return completed;
  }
  
  /**
   * Lists the accounts of the current user.
   * @return the list of the current user's accounts
   */
  public List<Account> listAccounts() {
    List<Account> accounts = null;
    
    if (this.currentCustomer != null && this.authenticated) {
      accounts = this.currentCustomer.getAccounts();
    }
    
    return accounts;
  }
  
  /**
   * Makes a deposit to one of the current customer's accounts.
   * @param amount the amount to be deposited
   * @param accountId the ID of the customer's account
   * @return true if the deposit was successful, false otherwise
   * @throws InvalidAccountException if the account was not found in the user's account
   */
  public boolean makeDeposit(BigDecimal amount, int accountId) throws InvalidAccountException {
    boolean completed = false;
    
    if (this.currentCustomer != null && this.authenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());
      
      // Checking if the account is of the customer's
      if (customerAccs.contains(accountId)) {
        BigDecimal currentBalance = DatabaseSelectHelper.getBalance(accountId);
        BigDecimal newBalance = currentBalance.add(amount);
        // Rounding
        newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
        // Updating
        DatabaseUpdateHelper.updateAccountBalance(newBalance, accountId);
        completed = true;
      } else {
        throw new InvalidAccountException();
      }
    }
    
    return completed;
  }
  
  /**
   * Checks the balance of one of the current customer's accounts.
   * @param accountId the ID of the customer's account
   * @return the balance of the account
   * @throws InvalidAccountException if the account was not found in the user's accounts
   */
  public BigDecimal checkBalance(int accountId) throws InvalidAccountException {
    BigDecimal balance = null;
    
    if (this.currentCustomer != null && this.authenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());
      
      // Checking if the account is of the customer's
      if (customerAccs.contains(accountId)) {
        balance = DatabaseSelectHelper.getBalance(accountId);
      } else {
        throw new InvalidAccountException();
      }
    }
    
    return balance;
  }
  
  /**
   * Makes a withdrawal from one of the current customer's account.
   * @param amount the amount to be withdrawn, which cannot exceed the current balance
   * @param accountId the ID of the account
   * @return true if the withdrawal was successful, false otherwise
   * @throws InsuffiecintFundsException if the resulting balance would be negative
   * @throws InvalidAccountException if the account was not found in the user's accounts
   * @throws InsufficientPermissionException if the customer is trying to withdraw from a Rsa
   */
  
  // TODO Check if restrictedsavingsaccount and throw InsufficientPermission exception
  public boolean makeWithdrawal(BigDecimal amount, int accountId) throws InsuffiecintFundsException,
      InvalidAccountException, InsufficientPermissionException {
    boolean completed = false;
    int rsaTypeId = AccountMap.getInstance().getTypeId("RESTRICTEDSAVING");
    
    if (this.currentCustomer != null && this.authenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());
      
      if (DatabaseSelectHelper.getAccountType(accountId) != rsaTypeId) {
        if (customerAccs.contains(accountId)) {
          // Checking if the customer owns the account
          BigDecimal currentBalance = DatabaseSelectHelper.getBalance(accountId);
          BigDecimal newBalance = currentBalance.subtract(amount);
          // Rounding
          newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
          
          if (newBalance.signum() != -1) {
            // Updating
            DatabaseUpdateHelper.updateAccountBalance(newBalance, accountId);
            completed = true;
          } else {
            throw new InsuffiecintFundsException();
          }
        } else {
          throw new InvalidAccountException();
        }
      } else {
        throw new InsufficientPermissionException();
      }
    }
    
    return completed;
  }
  
  /**
   * Views the message with Id messageId.
   * @param messageId of the wanted message
   * @return the message (String)
   */
  public String viewMessage(int messageId) {
    String specificMessage = DatabaseSelectHelper.getSpecificMessage(messageId);
    String result = "";
    // Get all the users messages
    List<Message> messages = DatabaseSelectHelper.getAllMessages(currentCustomer.getId());
    // Go through the users messages
    for (Message message: messages) {
      // Check that the message is the users
      if (message.getMessage().equals(specificMessage)) {
        // update viewed status and return the message
        DatabaseUpdateHelper.updateUserMessageState(messageId);
        result =  specificMessage;
      }
    }
    return result;
  }
}
