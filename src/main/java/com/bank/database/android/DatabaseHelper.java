package com.bank.database.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bank.accounts.Account;
import com.bank.accounts.BalanceOwingAccount;
import com.bank.accounts.ChequingAccount;
import com.bank.accounts.RestrictedSavingsAccount;
import com.bank.accounts.SavingsAccount;
import com.bank.accounts.Tfsa;
import com.bank.generics.AccountTypes;
import com.bank.generics.Roles;
import com.bank.messages.Message;
import com.bank.users.Admin;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by byron on 2017-07-27.
 */

public class DatabaseHelper extends DatabaseDriverA {
  private Context context;

  public DatabaseHelper(Context context) {
    super(context);
    this.context = context;
  }

  //region Initialization
  /**
   * Initializes the database.
   */
  public int initializeDatabase(String adminPassword) {
    this.insertAccountType("CHEQUING", new BigDecimal("0.01"));
    this.insertAccountType("SAVING", new BigDecimal("0.02"));
    this.insertAccountType("TFSA", new BigDecimal("0.03"));
    this.insertAccountType("RESTRICTEDSAVING", new BigDecimal("0.04"));
    this.insertAccountType("BALANCEOWING", new BigDecimal("0.05"));
    int adminRoleId = (int) this.insertRole("ADMIN");
    this.insertRole("TELLER");
    this.insertRole("CUSTOMER");
    return (int) this.insertNewUser("Default Admin", 19, "123 Admin Street",
            adminRoleId, adminPassword);
  }

  /**
   * If a database already exists, reinitialize it.
   */
  public void reinitializeDatabase() {
    DatabaseDriverA db = new DatabaseDriverA(context);
    SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
    db.onUpgrade(sqLiteDatabase, 1, 1);
    db.close();
  }
  //endregion

  //region Inserters
  /**
   * Inserts the userRoles to the database.
   */
  public long insertRole(String role) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long roleId = -1;

    if (!this.getRoleList().contains(role)) {
      try {
        Roles.valueOf(role);
      } catch (Exception e) {
        // role not in Roles enum, method will return -1
      }

      roleId = db.insertRole(role);
    }

    db.close();
    return roleId;
  }

  /**
   * Inserts the account into the database.
   */
  public long insertAccount(String name, BigDecimal balance, int typeId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long id = -1;

    if (balance.scale() == 2 && !name.isEmpty() && this.getAccountTypeIds().contains(typeId)) {
      id = db.insertAccount(name, balance, typeId);
    }

    db.close();
    return id;
  }

  /**
   * Insert a new user/account relationship.
   */
  public long insertUserAccount(int userId, int accountId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long id = -1;

    if (this.getUserIds().contains(userId)) {
      id = db.insertUserAccount(userId, accountId);
    }

    db.close();
    return id;
  }

  /**
   * Inserts the account types into the database.
   */
  public long insertAccountType(String type, BigDecimal interestRate) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long id = -1;

    try {
      if (interestRate.compareTo(new BigDecimal("1")) <= 0 && interestRate.signum() >= 0) {
        // will throw exception if account type is not in enum
        AccountTypes.valueOf(type);

        for (int typeId : this.getAccountTypeIds()) {
          if (db.getAccountTypeName(typeId).equals(type)) {
            // throw exception if account type already exists
            throw new Exception();
          }
        }
      }

      id = db.insertAccountType(type, interestRate);
    } catch (Exception e) {
      // method will return -1
    }

    db.close();
    return id;
  }

  /**
   * Inserts a new user into the database.
   */
  public long insertNewUser(String name, int age, String address, int roleId, String password) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long id = -1;

    if (!name.isEmpty() && age >= 0 && address.length() <= 100 && !address.isEmpty()
        && this.getRoleIds().contains(roleId) && !password.isEmpty()) {
      // Inserting user
      id = db.insertNewUser(name, age, address, roleId, password);
    }

    db.close();
    return id;
  }

  /**
   * Inserts a message into the database.
   */
  public long insertMessage(int userId, String message) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    long messageId = -1;

    if (!message.isEmpty() && message.length() <= 512 && this.getUserIds().contains(userId)) {
      messageId = db.insertMessage(userId, message);
    }

    db.close();
    return messageId;
  }
  //endregion

  //region Selectors
  /**
   * Gets the Name of a RoleId.
   * @param roleId id of the role
   * @return name of the role
   */
  public String getRoleName(int roleId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    String roleName = null;

    try {
      roleName = db.getRole(roleId);
    } catch (Exception e) {
      // method will return null if role does not exist
    }

    db.close();
    return roleName;
  }

  /**
   * Gets a list of the roleIds in the database.
   * @return list of roleIds
   */
  public List<Integer> getRoleIds() {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = db.getRoles();
    List<Integer> roles = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        roles.add(cursor.getInt(cursor.getColumnIndex("ID")));
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return roles;
  }
  
  /**
   * Gets a list of the roles from the database.
   * @return a list of roles
   */
  public List<String> getRoleList() {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = db.getRoles();
    List<String> roles = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        roles.add(cursor.getString(cursor.getColumnIndex("NAME")));
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return roles;
  }

  /**
   * Gets a list of AccountTypeIds from the database.
   * @return list of AccountTypeIds
   */
  public List<Integer> getAccountTypeIds() {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = db.getAccountTypesId();
    List<Integer> types = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        types.add(cursor.getInt(cursor.getColumnIndex("ID")));
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return types;
  }

  /**
   * Gets a list of UserIds.
   * @return list of userids
   */
  public List<Integer> getUserIds() {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = db.getUsersDetails();
    List<Integer> userIds = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        userIds.add(cursor.getInt(cursor.getColumnIndex("ID")));
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return userIds;
  }

  /**
   * Gets the password of the given user.
   */
  public String getPassword(int userId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    String password = null;

    try {
      password = db.getPassword(userId);
    } catch (Exception e) {
      // method will return null if user does not exist
    }

    db.close();
    return password;
  }

  /**
  * Gets the specified user.
  * @param userId id of the user
  * @return User oject of the user
  */
  public User getUserObject(int userId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = null;
    User user = null;

    try {
      cursor = db.getUserDetails(userId);
    } catch (Exception e) {
      // method will return null if user doesn't exist
    }

    // Creating the User
    if (cursor != null && cursor.moveToFirst()) {
      do {
        String name = cursor.getString(cursor.getColumnIndex("NAME"));
        int roleId = cursor.getInt(cursor.getColumnIndex("ROLEID"));
        String roleName = this.getRoleName(roleId);
        int age = cursor.getInt(cursor.getColumnIndex("AGE"));
        String address = cursor.getString(cursor.getColumnIndex("ADDRESS"));
        switch (roleName) {
          case "ADMIN":
            user = new Admin(
                this.context,
                userId,
                name,
                age,
                address);
            break;
          case "TELLER":
            user = new Teller(
                this.context,
                userId,
                name,
                age,
                address);
            break;
          case "CUSTOMER":
            user = new Customer(
                this.context,
                userId,
                name,
                age,
                address);
            break;
          default:
            // SQLException would have been thrown if an ID was invalid
        }
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();
    return user;
  }
  
  /**
   * Gets a list of the UserAccountIds.
   * @param userId of the user
   * @return a list of the user's account Ids
   */
  public List<Integer> getUsersAccIds(int userId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = null;
    List<Integer> accountIds = null;

    try {
      cursor = db.getAccountIds(userId);

      // adding account IDs to list
      accountIds = new ArrayList<>();
      if (cursor.moveToFirst()) {
        do {
          accountIds.add(cursor.getInt(cursor.getColumnIndex("ACCOUNTID")));
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      // method will return null if user doesn't exist
    }

    cursor.close();
    db.close();
    return accountIds;
  }

  /**
   * Gets the roletype of the specified user.
   */
  public int getUserRole(int userId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    int role = -1;

    try {
      role = db.getUserRole(userId);
    } catch (Exception e) {
      // method will return null if user does not exist
    }

    db.close();
    return role;
  }

  /**
   * Gets an account object with the given accountId.
   * @param accountId id of the desired account
   * @return Account object of the desired account
   */
  public Account getAccountObject(int accountId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = null;
    Account account = null;

    try {
      cursor = db.getAccountDetails(accountId);
    } catch (Exception e) {
      // method will return null if user doesn't exist
    }

    // Creating the Account
    if (cursor != null && cursor.moveToFirst()) {
      do {
        String name = cursor.getString(cursor.getColumnIndex("NAME"));
        BigDecimal balance = new BigDecimal(cursor.getString(cursor.getColumnIndex("BALANCE")));

        switch (this.getAccountTypeName(cursor.getInt(cursor.getColumnIndex("TYPE")))) {
          case "CHEQUING":
            account = new ChequingAccount(
                this.context,
                accountId,
                name,
                balance);
            break;
          case "SAVING":
            account = new SavingsAccount(
                this.context,
                accountId,
                name,
                balance);
            break;
          case "TFSA":
            account = new Tfsa(
                this.context,
                accountId,
                name,
                balance);
            break;
          case "RESTRICTEDSAVING":
            account = new RestrictedSavingsAccount(
                this.context,
                accountId,
                name,
                balance);
            break;
          case "BALANCEOWING":
            account = new BalanceOwingAccount(
                this.context,
                accountId,
                name,
                balance);
            break;
          default:
            // SQLException would have been thrown if an ID was invalid
        }
      } while (cursor.moveToNext());
    }

    return account;
  }

  /**
   * Gets the accounttype of the given accountId.
   */
  public int getAccountType(int accountId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    int type = -1;

    try {
      type = db.getAccountType(accountId);
    } catch (Exception e) {
      // method will return null if account does not exist
    }

    db.close();
    return type;
  }

  /**
   * Gets the name of the given accounttype.
   */
  public String getAccountTypeName(int typeId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    String name = null;

    try {
      name = db.getAccountTypeName(typeId);
    } catch (Exception e) {
      // method will return null if account type doesn't exist
    }

    db.close();
    return name;
  }

  /**
   * Gets the balace of the given accountId.
   */
  public BigDecimal getBalance(int accountId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    BigDecimal balance = null;

    try {
      balance = db.getBalance(accountId);
    } catch (Exception e) {
      // method will return null if account doesn't exist
    }

    db.close();
    return balance;
  }

  /**
   * Gets the interestrate of the accounttype.
   */
  public BigDecimal getInterestRate(int type) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    BigDecimal interestRate = null;

    try {
      interestRate = db.getInterestRate(type);
    } catch (Exception e) {
      // method will return null if account type doesn't exist
    }

    db.close();
    return interestRate;
  }

  /**
   * Gets a list of messages from the given userId.
   * @param userId id of the user we are getting messages from
   * @return list of messages
   */
  public List<Message> getMessageList(int userId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    Cursor cursor = null;
    List<Message> messages = null;

    try {
      cursor = db.getAllMessages(userId);

      messages = new ArrayList<>();
      if (cursor != null && cursor.moveToFirst()) {
        do {
          int messageId = cursor.getInt(cursor.getColumnIndex("ID"));
          int userMsgId = cursor.getInt(cursor.getColumnIndex("USERID"));
          String messageStr = cursor.getString(cursor.getColumnIndex("MESSAGE"));
          int viewed = cursor.getInt(cursor.getColumnIndex("VIEWED"));

          Message message = new Message(messageId, userMsgId, messageStr, viewed);
          messages.add(message);
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      // method will return null if user doesn't exist
    }

    cursor.close();
    db.close();
    return messages;
  }

  /**
   * Get a specific message with the given messageId.
   */
  public String getSpecificMessage(int messageId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    String message = null;

    try {
      message = db.getSpecificMessage(messageId);
    } catch (Exception e) {
      // method will return null if message doesn't exist
    }

    db.close();
    return message;
  }

  //endregion

  //region Updaters
  /**
   * Updates the given account's balance to the given balance amount.
   */
  public boolean updateAccountBalance(BigDecimal balance, int accountId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean success = false;

    if (balance.scale() == 2) {
      try {
        success = db.updateAccountBalance(balance, accountId);
      } catch (Exception e) {
        // method will return false if account ID does not exist
      }
    }

    db.close();
    return success;
  }

  /**
   * Updates the given user's message status.
   */
  public boolean updateUserMessageState(int messageId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    try {
      updated = db.updateUserMessageState(messageId);
    } catch (Exception e) {
      // method will return false if message doesn't exist
    }

    db.close();
    return updated;
  }

  /**
   * Updates the name of the given user.
   * @param userId id of the user whos name is being updated
   * @param name new name
   * @return true if success, false otherwise
   */
  public boolean updateUserName(int userId, String name) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    if (!name.isEmpty()) {
      try {
        updated = db.updateUserName(name, userId);
      } catch (Exception e) {
        // method will return false if user doesn't exist
      }
    }

    db.close();
    return updated;
  }

  /**
   * Updates the age of the given user.
   * @param userId id of the user whos age is being updated
   * @param age new age
   * @return true if success, false otherwise
   */
  public boolean updateUserAge(int userId, int age) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    if (age >= 0) {
      try {
        updated = db.updateUserAge(age, userId);
      } catch (Exception e) {
        // method will return false if user doesn't exist
      }
    }

    db.close();
    return updated;
  }

  /**
   * Updates the address of the given user.
   * @param userId id of the user whos address is being updated
   * @param address new address
   * @return true if success, false otherwise
   */
  public boolean updateUserAddress(int userId, String address) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    if (address.length() <= 100) {
      try {
        updated = db.updateUserAddress(address, userId);
      } catch (Exception e) {
        // method will return false if user doesn't exist
      }
    }

    db.close();
    return updated;
  }

  /**
   * Updates the password of the given user.
   * @param userId id of the user whos password is being updated
   * @param password new password
   * @return true if success, false otherwise
   */
  public boolean updateUserPassword(int userId, String password) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    if (!password.isEmpty()) {
      try {
        updated = db.updateUserPassword(password, userId);
      } catch (Exception e) {
        // method will return false if user doesn't exist
      }
    }

    db.close();
    return updated;
  }

  /**
   * Updates the role of the given user.
   * @param userId id of the user whos role is being updated
   * @param roleId new role
   * @return true if success, false otherwise
   */
  public boolean updateUserRole(int userId, int roleId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean updated = false;

    if (this.getRoleIds().contains(roleId)) {
      try {
        updated = db.updateUserRole(roleId, userId);
      } catch (Exception e) {
        // method will return false if user doesn't exist
      }
    }

    db.close();
    return updated;
  }

  /**
   * Updates the type of the given account.
   * @param accountId id of the account whos type is being updated
   * @param typeId new type
   * @return true if success, false otherwise
   */
  public boolean updateAccountType(int accountId, int typeId) {
    DatabaseDriverA db = new DatabaseDriverA(context);
    boolean success = false;

    if (this.getAccountTypeIds().contains(typeId)) {
      try {
        success = db.updateAccountType(typeId, accountId);
      } catch (Exception e) {
        // method will return false if account ID does not exist
      }
    }

    db.close();
    return success;
  }
  //endregion
}
