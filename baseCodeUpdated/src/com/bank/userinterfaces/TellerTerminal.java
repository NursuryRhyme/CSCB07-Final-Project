package com.bank.userinterfaces;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.IllegalAgeException;
import com.bank.exceptions.IllegalAmountException;
import com.bank.exceptions.InsuffiecintFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.users.Customer;
import com.bank.users.User;
import java.math.BigDecimal;
import java.util.List;

public class TellerTerminal extends Atm {
  private User currentUser = null;
  private boolean currentUserAuthenticated = false;
  private Customer currentCustomer = null;
  private boolean currentCustomerAuthenticated = false;

  /**
   * Creates a TellerTerminal object.
   *
   * @param tellerId the ID of the teller
   * @param password the input password
   */
  public TellerTerminal(int tellerId, String password) {
    super(tellerId);
    this.currentUser = DatabaseSelectHelper.getUserDetails(tellerId);

    if (this.currentUser != null) {
      this.currentUserAuthenticated = currentUser.authenticated(password);
    }
  }

  /**
   * Makes a new account and registers it to the current customer.
   *
   * @param name    a nonempty name of the account
   * @param balance the balance of the account with 2 decimal places
   * @param type    the type of account from the AccountType enumerator
   * @return the ID of the new UserAccount, -1 otherwise
   */
  public int makeNewAccount(String name, BigDecimal balance, int type) {
    int ret = -1;

    if (this.currentUser != null && this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      int accountId = DatabaseInsertHelper.insertAccount(name, balance, type);

      // Checking if the account was inserted to the database
      if (accountId != -1) {
        ret = DatabaseInsertHelper.insertUserAccount(this.currentCustomer.getId(), accountId);
        this.currentCustomer.addAccount(DatabaseSelectHelper.getAccountDetails(accountId));
      }
    }

    return ret;
  }

  /**
   * Sets the current customer.
   *
   * @param customer the customer
   */
  public void setCurrentCustomer(Customer customer) {
    if (this.currentUserAuthenticated) {
      this.currentCustomer = customer;
      this.currentCustomerAuthenticated = false;
    }
  }

  public Customer getCurrentCustomer() {
    return this.currentCustomer;
  }

  /**
   * Authenticate the current customer with the input password.
   *
   * @param password the input password
   */
  public void authenticateCurrentCustomer(String password) {
    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      this.currentCustomerAuthenticated = this.currentCustomer.authenticated(password);
    }
  }

  /**
   * Creates a new user.
   *
   * @param name     the name of the user
   * @param age      the age of the user
   * @param address  the address of the user
   * @param password the user's password
   * @return the new user's ID if created, -1 otherwise
   */
  public int makeNewUser(String name, int age, String address, String password)
      throws IllegalAgeException {
    int userId = -1;
    int customerRoleId = RoleMap.getInstance().getRoleId("CUSTOMER");

    if (this.currentUserAuthenticated) {
      userId = DatabaseInsertHelper.insertNewUser(name, age, address, customerRoleId, password);
      this.currentCustomer = (Customer) DatabaseSelectHelper.getUserDetails(userId);
      this.authenticateCurrentCustomer(password);
    }

    return userId;
  }

  /**
   * Give interest to one of the curren It will be due on July 31st with
   * your final submission, but should be accounted for in your updated UML
   * user's account if authenticated.
   *
   * @param accountId the ID of the account
   */
  public void giveInterest(int accountId) {
    if (this.currentUserAuthenticated && this.currentCustomerAuthenticated) {
      List<Integer> customerAccIds
          = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());

      if (customerAccIds.contains(accountId)) {
        // Loop through the customer's accounts to find the matching account
        // and give interest based on the type
        for (Account account : this.currentCustomer.getAccounts()) {
          if (account.getId() == accountId) {
            account.findAndSetInterestRate();
            account.addInterest();
          }
        }
      }
    }
  }

  public void deAuthenticateCustomer() {
    this.currentCustomerAuthenticated = false;
    this.currentCustomer = null;
  }

  @Override public List<Account> listAccounts() {
    List<Account> accounts = null;

    // Checking that the customer is authenticated
    if (this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      accounts = this.currentCustomer.getAccounts();
    }

    return accounts;
  }

  @Override public boolean makeDeposit(BigDecimal amount, int accountId)
      throws InvalidAccountException {
    boolean completed = false;

    // Checking that the customer is authenticated
    if (this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());

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

  @Override public BigDecimal checkBalance(int accountId) throws InvalidAccountException {
    BigDecimal balance = null;

    // Checking that the customer is authenticated
    if (this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());

      if (customerAccs.contains(accountId)) {
        balance = DatabaseSelectHelper.getBalance(accountId);
      } else {
        throw new InvalidAccountException();
      }
    }

    return balance;
  }

  @Override public boolean makeWithdrawal(BigDecimal amount, int accountId)
      throws InsuffiecintFundsException, InvalidAccountException {
    boolean completed = false;

    if (this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      List<Integer> customerAccs = DatabaseSelectHelper.getAccountIds(this.currentCustomer.getId());

      if (customerAccs.contains(accountId)) {
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
    }

    return completed;
  }

  /**
   * Creates a message for the recipient.
   * @param recipientId the UserId of the recipient
   * @param message the contents of the message
   * @return the id of the message
   */
  public int createMessage(int recipientId, String message) {
    int messageId = -1;
    // check authentication and if recipient is a customer
    if (this.currentUserAuthenticated && (DatabaseSelectHelper.getUserRole(recipientId)
        == RoleMap.getInstance().getRoleId("CUSTOMER"))) {
      try {
        messageId = DatabaseInsertHelper.insertMessage(recipientId, message);
      } catch (IllegalAmountException e) {
        e.printStackTrace();
      }
    }
    return messageId;
  }

  /**
   * Views the message with id messageId.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewMessage(int messageId) {

    // Check authentication
    if (this.currentUserAuthenticated) {
      String specificMessage = DatabaseSelectHelper.getSpecificMessage(messageId);
      String result = "";
      // Get all the Teller's messages
      List<Message> messages = DatabaseSelectHelper.getAllMessages(currentUser.getId());
      // Go through the Teller's messages
      for (Message message : messages) {
        // Check that the message is the Teller's
        if (message.getMessage().equals(specificMessage)) {
          // update viewed status and return the message
          DatabaseUpdateHelper.updateUserMessageState(messageId);
          result = specificMessage;
        }
      }
      return result;
    } else {
      return null;
    }
  }

  /**
   * View the currentCustomer's messages.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewCustomersMessage(int messageId) {
    // Check authentication
    if (this.currentUserAuthenticated && this.currentCustomerAuthenticated) {
      String specificMessage = DatabaseSelectHelper.getSpecificMessage(messageId);
      String result = "";
      // Get all the Customer's messages
      List<Message> messages = DatabaseSelectHelper.getAllMessages(currentCustomer.getId());
      // Go through the Customer's messages
      for (Message message : messages) {
        // Check that the message is the Customer's
        if (message.getMessage().equals(specificMessage)) {
          // update viewed status and return the message
          DatabaseUpdateHelper.updateUserMessageState(messageId);
          result = specificMessage;
        }
      }
      return result;
    } else {
      return null;
    }
  }
}
