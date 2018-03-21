package com.bank.userinterfaces;

import android.content.Context;

import com.bank.accounts.Account;
import com.bank.database.android.DatabaseHelper;
import com.bank.exceptions.IllegalAgeException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPermissionException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.generics.AccountMap;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.security.PasswordHelpers;
import com.bank.users.Customer;
import com.bank.users.User;
import java.math.BigDecimal;
import java.util.List;

public class TellerTerminal extends Atm {
  private User currentUser = null;
  private boolean currentUserAuthenticated = false;
  private Customer currentCustomer = null;
  private boolean currentCustomerAuthenticated = false;
  private Context context;
  private DatabaseHelper dbHelper;
  private AccountMap accountMap;

  /**
   * Creates a TellerTerminal object.
   *
   * @param tellerId the ID of the teller
   * @param password the input password
   */
  public TellerTerminal(Context context, int tellerId, String password) {
    super(context, tellerId);
    this.context = context;
    dbHelper = new DatabaseHelper(context);
    accountMap = AccountMap.getInstance(context);
    this.currentUser = dbHelper.getUserObject(tellerId);

    if (this.currentUser != null) {
      this.currentUserAuthenticated = currentUser.authenticated(password);
    }
  }

  /**
   * Makes a new account and registers it to the current customer.
   *
   * @param name  a nonempty name of the account
   * @param balance the balance of the account with 2 decimal places
   * @param type  the type of account from the AccountType enumerator
   * @return the ID of the new UserAccount, -1 otherwise
   */

  public long makeNewAccount(String name, BigDecimal balance, int type) {
    long accountId = -1;

    if (this.currentUser != null && this.currentCustomer != null && this.currentUserAuthenticated
        && this.currentCustomerAuthenticated) {
      accountId = dbHelper.insertAccount(name, balance, type);
      int custId = this.currentCustomer.getId();
      // Checking if the account was inserted to the database
      if (accountId != -1) {
        dbHelper.insertUserAccount(custId, (int) accountId);
        this.currentCustomer.addAccount(dbHelper.getAccountObject((int) accountId));
      }
    }

    return accountId;
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
  public boolean authenticateCurrentCustomer(String password) {
    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      this.currentCustomerAuthenticated = this.currentCustomer.authenticated(password);
    }

    return this.currentCustomerAuthenticated;
  }

  /**
   * Creates a new user.
   *
   * @param name   the name of the user
   * @param age    the age of the user
   * @param address  the address of the user
   * @param password the user's password
   * @return the new user's ID if created, -1 otherwise
   */
  public int makeNewUser(String name, int age, String address, String password)
      throws IllegalAgeException {
    int userId = -1;
    int customerRoleId = RoleMap.getInstance(context).getRoleId("CUSTOMER");

    if (this.currentUserAuthenticated) {
      userId = (int)dbHelper.insertNewUser(name, age, address, customerRoleId, password);
      this.currentCustomer = (Customer) dbHelper.getUserObject(userId);
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
      List<Integer> customerAccIds = dbHelper.getUsersAccIds(this.currentCustomer.getId());

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

  /**
   * Give interest to all of the current customer's accounts.
   */
  public void giveAllInterest() {
    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      for (Account account : listAccounts()) {
        account.findAndSetInterestRate();
        account.addInterest();
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
        //&& this.currentCustomerAuthenticated
        ) {
      accounts = this.currentCustomer.getAccounts();
    }

    return accounts;
  }

  @Override public boolean makeDeposit(BigDecimal amount, int accountId)
      throws InvalidAccountException {
    boolean completed = false;

    // Checking that the customer is authenticated
    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());

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

  @Override public BigDecimal checkBalance(int accountId) throws InvalidAccountException {
    BigDecimal balance = null;

    // Checking that the customer is authenticated
    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());

      if (customerAccs.contains(accountId)) {
        balance = dbHelper.getBalance(accountId);
      } else {
        throw new InvalidAccountException();
      }
    }

    return balance;
  }

  @Override public boolean makeWithdrawal(BigDecimal amount, int accountId) throws
          InsufficientFundsException, InvalidAccountException, InsufficientPermissionException {
    boolean completed = false;

    if (this.currentCustomer != null && this.currentUserAuthenticated) {
      List<Integer> customerAccs = dbHelper.getUsersAccIds(this.currentCustomer.getId());

      if (customerAccs.contains(accountId)) {
        BigDecimal currentBalance = dbHelper.getBalance(accountId);
        BigDecimal newBalance = currentBalance.subtract(amount);
        // Rounding
        newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);

        int type = dbHelper.getAccountType(accountId);

        RoleMap roleMap = RoleMap.getInstance(context);

        if (type == accountMap.getTypeId("RESTRICTEDSAVING")) {
          if (dbHelper.getUserRole(this.currentUser.getId()) == roleMap.getRoleId("CUSTOMER")) {
            throw new InsufficientPermissionException();
          }
        }

        if (newBalance.signum() != -1) {
          // Switching account to savings if new balance is less than 1000
          if (type == accountMap.getTypeId("SAVING")) {
            if (newBalance.compareTo(new BigDecimal("1000")) == -1) {
              dbHelper.updateAccountType(accountId, accountMap.getTypeId("CHEQUING"));
              createMessage(this.currentCustomer.getId(), "Your savings account ID "
                  + accountId + " has become a chequing account due to going "
                      + "below the minimum balance of $1000.");
            }
          }

          // Updating
          dbHelper.updateAccountBalance(newBalance, accountId);
          completed = true;
        } else {
          throw new InsufficientFundsException();
        }

      } else {
        throw new InvalidAccountException();
      }
    }

    return completed;
  }

  /**
   * Creates a message.
   * @param recipientId the UserId of the recipient
   * @param message   the contents of the message
   * @return the id of the message
   */
  public int createMessage(int recipientId, String message) {
    int messageId = -1;
    // check authentication and if recipient is a customer
    User user = dbHelper.getUserObject(recipientId);

    if (this.currentUserAuthenticated && user != null && (user.getRoleId()
        == RoleMap.getInstance(context).getRoleId("CUSTOMER"))) {
      try {
        messageId = (int)dbHelper.insertMessage(recipientId, message);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return messageId;
  }

  /**
   * View the message with the given messageId.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewMessage(int messageId) {

    // Check authentication
    if (this.currentUserAuthenticated) {
      String specificMessage = dbHelper.getSpecificMessage(messageId);
      String result = "";
      // Get all the Teller's messages
      List<Message> messages = dbHelper.getMessageList(currentUser.getId());
      // Go through the Teller's messages
      for (Message message : messages) {
        // Check that the message is the Teller's
        if (message.getMessage().equals(specificMessage)) {
          // update viewed status and return the message
          dbHelper.updateUserMessageState(messageId);
          result = specificMessage;
        }
      }
      return result;
    } else {
      return null;
    }
  }

  /**
   * View the customer's messages.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewCustomersMessage(int messageId) {
    // Check authentication
    if (this.currentUserAuthenticated && this.currentCustomerAuthenticated) {
      String specificMessage = dbHelper.getSpecificMessage(messageId);
      String result = "";
      // Get all the Customer's messages
      List<Message> messages = dbHelper.getMessageList(currentCustomer.getId());
      // Go through the Customer's messages
      for (Message message : messages) {
        // Check that the message is the Customer's
        if (message.getMessage().equals(specificMessage)) {
          // update viewed status and return the message
          dbHelper.updateUserMessageState(messageId);
          result = specificMessage;
        }
      }
      return result;
    } else {
      return null;
    }
  }

  /**
   * Get a list of messages from the given userId.
   * @param userId of the person you wanna see messages from
   * @return list of messages
   */
  public List<Message> listMessages(int userId) {
    List<Message> messages = dbHelper.getMessageList((userId));

    if (messages != null) {
      for (Message message : messages) {
        dbHelper.updateUserMessageState(message.getId());
      }
    }

    return messages;
  }

  /**
   * Updates the current customer's information.
   * @param name new name of the customer
   * @param age new age of the customer
   * @param address new address of the customer
   * @param password new password of the customer
   * @return true if updated, false otherwise
   */
  public boolean updateCustomerInfo(String name, int age, String address, String password) {
    int customerId = this.currentCustomer.getId();

    // Updating info
    boolean nameSuccess = dbHelper.updateUserName(customerId, name);
    boolean ageSuccess = dbHelper.updateUserAge(customerId, age);
    boolean addrSuccess = dbHelper.updateUserAddress(customerId, address);
    String hashedPassword = PasswordHelpers.passwordHash(password);
    boolean pwSuccess = dbHelper.updateUserPassword(customerId, hashedPassword);

    if (nameSuccess && ageSuccess && addrSuccess && pwSuccess) {
      this.currentCustomer.setName(name);
      this.currentCustomer.setAge(age);
      this.currentCustomer.setAddress(address);
      return true;
    }

    return false;
  }

  /**
   * Transfers money from one account to another.
   * @param fromAccount account money is coming from
   * @param toAccount account money is going to
   * @param amount the amount of money
   * @return true if money was sent, false otherwise
   * @throws InsufficientFundsException if source account does not have enough funds
   * @throws InsufficientPermissionException if user does not have permission
   * @throws InvalidAccountException if the source account does not exist
   */
  public boolean sendMoney(int fromAccount, int toAccount, BigDecimal amount) throws
          InsufficientFundsException, InsufficientPermissionException, InvalidAccountException {
    boolean completed = false;

    this.makeWithdrawal(amount, fromAccount);

    if (this.currentUserAuthenticated) {
      BigDecimal currentBalance = dbHelper.getBalance(toAccount);
      BigDecimal newBalance = currentBalance.add(amount);
      // Rounding
      newBalance = newBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
      // Updating
      completed = dbHelper.updateAccountBalance(newBalance, toAccount);
    }

    return completed;
  }

  /**
   * Returns the total balance of the current customer.
   * @return the total balance
   */
  public BigDecimal customerTotalBalance() {
    BigDecimal total = new BigDecimal("0");

    if (this.currentUserAuthenticated) {
      for (int accountId : dbHelper.getUsersAccIds(this.currentCustomer.getId())) {
        total = total.add(dbHelper.getBalance(accountId));
      }
    }

    return total;
  }
}
