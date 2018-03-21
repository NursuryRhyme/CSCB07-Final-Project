package com.bank.userinterfaces;

import android.content.Context;

import com.bank.accounts.Account;
import com.bank.database.android.DatabaseHelper;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPermissionException;
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
  private Context context;
  private DatabaseHelper dbHelper;

  /**
   * Creates an ATM object.
   * @param customerId the ID of a user in the database.
   * @param password the password for the user
   */
  public Atm(Context context, int customerId, String password) {
    this.context = context;
    dbHelper = new DatabaseHelper(context);
    User user = dbHelper.getUserObject(customerId);

    // Checking if the customer is a Customer object
    if (user.getRoleId() == RoleMap.getInstance(this.context).getRoleId("CUSTOMER")) {
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
  public Atm(Context context, int customerId) {
    dbHelper = new DatabaseHelper(context);
    User user = dbHelper.getUserObject(customerId);

    // Checking if the customer is a Customer object
    if (user.getRoleId() == RoleMap.getInstance(this.context).getRoleId("CUSTOMER")) {
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
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());


      // Checking if the account is of the customer's
      if (customerAccs.contains(accountId)) {
        BigDecimal currentBalance = dbHelper.getBalance(accountId);
        BigDecimal newBalance = currentBalance.add(amount);
        // Rounding
        newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
        // Updating
        dbHelper.updateAccountBalance(newBalance, accountId);
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
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());

      // Checking if the account is of the customer's
      if (customerAccs.contains(accountId)) {
        balance = dbHelper.getBalance(accountId);
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
   * @throws InsufficientFundsException if the resulting balance would be negative
   * @throws InvalidAccountException if the account was not found in the user's accounts
   * @throws InsufficientPermissionException if the customer is trying to withdraw from a Rsa
   */

  public boolean makeWithdrawal(BigDecimal amount, int accountId) throws InsufficientFundsException,
      InvalidAccountException, InsufficientPermissionException {
    boolean completed = false;
    int rsaTypeId = AccountMap.getInstance(context).getTypeId("RESTRICTEDSAVING");

    if (this.currentCustomer != null && this.authenticated) {
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());
      Account account = dbHelper.getAccountObject(accountId);
      if (account.getType() != rsaTypeId) {
        if (customerAccs.contains(accountId)) {
          // Checking if the customer owns the account
          BigDecimal currentBalance = dbHelper.getBalance(accountId);
          BigDecimal newBalance = currentBalance.subtract(amount);
          // Rounding
          newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);

          if (newBalance.signum() != -1) {
            // Updating
            dbHelper.updateAccountBalance(newBalance, accountId);
            completed = true;
          } else {
            throw new InsufficientFundsException();
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
   * Views the message with the given messageId.
   * @param messageId of the message
   * @return the message
   */
  public String viewMessage(int messageId) {
    String specificMessage = dbHelper.getSpecificMessage(messageId);
    String result = "";
    // Get all the users messages
    List<Message> messages = dbHelper.getMessageList(currentCustomer.getId());
    // Go through the users messages
    for (Message message: messages) {
      // Check that the message is the users
      if (message.getMessage().equals(specificMessage)) {
        // update viewed status and return the message
        dbHelper.updateUserMessageState(messageId);
        result =  specificMessage;
      }
    }
    return result;
  }
  
  /**
   * List messages of the current Customer.
   * @return a iist of messages
   */
  public List<Message> listMyMessages() {
    List<Message> messages = dbHelper.getMessageList((this.currentCustomer.getId()));

    if (messages != null) {
      for (Message message : messages) {
        dbHelper.updateUserMessageState(message.getId());
      }
    }

    return messages;
  }

  /**
   * Returns the current customer.
   * @return the current customer
   */
  public Customer getCustomer() {
    return this.currentCustomer;
  }
}
