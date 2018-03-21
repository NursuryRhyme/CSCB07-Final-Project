package com.bank.database.android;

import android.content.Context;

import com.bank.messages.Message;
import com.bank.users.User;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BankData implements Serializable {
  private static final long serialVersionUID = 1L;
  public List<UserSer> users = new ArrayList<UserSer>();
  public List<AccTypeSer> accountTypes = new ArrayList<AccTypeSer>();
  public List<RoleSer> roles = new ArrayList<RoleSer>();
  public List<AccountSer> accounts = new ArrayList<AccountSer>();
  public List<UserAccSer> userAccounts = new ArrayList<UserAccSer>();
  public List<UserMsgSer> userMessages = new ArrayList<UserMsgSer>();
  private transient DatabaseHelper dbHelper;

  /**
   * Creates a BankData object containing the information of a bank database.
   * */
  public BankData(Context context) {
    dbHelper = new DatabaseHelper(context);
    updateAccountTypes();
    updateRoles();
    updateUsersAndAccounts();
  }

  public class UserSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;
    public int age;
    public String address;
    public int roleId;
    public String password;
    
    /**
     * Creates a serializable representation of a user.
     * @param userId the id of the user
     * @param name the name of the user
     * @param age the age of the user
     * @param address the user's address
     * @param roleId the role of the user
     * @param password the user's password
     */
    public UserSer(int userId, String name, int age, String address, int roleId, String password) {
      this.id = userId;
      this.name = name;
      this.age = age;
      this.address = address;
      this.roleId = roleId;
      this.password = password;
    }
  }
  
  public class AccTypeSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;
    public BigDecimal interestRate = null;

    /**
     * Creates a serializable representation of an account type.
     * @param typeId id of the type
     * @param name name of the type
     * @param interestRate interest rate of the account
     */
    public AccTypeSer(int typeId, String name, BigDecimal interestRate) {
      this.id = typeId;
      this.name = name;
      this.interestRate = interestRate;
    }
  }
  
  public class RoleSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;

    public RoleSer(int typeId, String name) {
      this.id = typeId;
      this.name = name;
    }
  }
  
  public class AccountSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;
    public BigDecimal balance;
    public int type;
    
    /**
     * Creates a serializable representation of an account.
     * @param id the id of the account
     * @param name the name of the account
     * @param balance the balance of the account
     * @param typeId the type of the account
     */
    public AccountSer(int id, String name, BigDecimal balance, int typeId) {
      this.id = id;
      this.name = name;
      this.balance = balance;
      this.type = typeId;
    }
  }
  
  public class UserAccSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int userId;
    public int accountId;
    
    /**
     * Creates a serializable representation of an user account.
     * @param userId the user ID of the account owner
     * @param accountId the ID of the account
     */
    public UserAccSer(int userId, int accountId) {
      this.userId = userId;
      this.accountId = accountId;
    }
  }
  
  public class UserMsgSer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public int userId;
    public String message;
    public int viewed;
    
    /**
     * Creates a serializable representation of an user account.
     * @param id the ID of the message
     * @param userId the user ID of the account owner
     * @param message the message
     * @param viewed 1 if viewed, 0 otherwise
     */
    public UserMsgSer(int id, int userId, String message, int viewed) {
      this.id = id;
      this.userId = userId;
      this.message = message;
      this.viewed = viewed;
    }
  }
  
  private void updateUsersAndAccounts() {
    for (int userId : dbHelper.getUserIds()) {
      // Getting serializable user object
      User user = dbHelper.getUserObject(userId);
      String password = dbHelper.getPassword(userId);
      UserSer userSer = new UserSer(user.getId(), user.getName(), user.getAge(), 
          user.getAddress(), user.getRoleId(), password);
      users.add(userSer);
      
      // Adding serializable account and user account objects to its list
      for (int accountId : dbHelper.getUsersAccIds(userId)) {
        // Adding account objects to account list
        String name = dbHelper.getAccountObject(accountId).getName();
        BigDecimal balance = dbHelper.getBalance(accountId);
        int type = dbHelper.getAccountType(accountId);
        
        // Adding serializable account object to account list
        AccountSer accountSer = new AccountSer(accountId, name, balance, type);
        accounts.add(accountSer);
        
        // Adding user accounts
        UserAccSer userAccSer = new UserAccSer(userId, accountId);
        userAccounts.add(userAccSer);
      }
      
      // Adding serializable user message objects to its list
      for (Message messageObj : dbHelper.getMessageList(userId)) {
        int id = messageObj.getId();
        String message = messageObj.getMessage();
        int msgUserId = messageObj.getUserId();
        int viewed = messageObj.getViewed();
        
        UserMsgSer userMsgSer = new UserMsgSer(id, msgUserId, message, viewed);
        userMessages.add(userMsgSer);
      }
    }
  }
  
  private void updateAccountTypes() {
    for (int typeId : dbHelper.getAccountTypeIds()) {
      String name = dbHelper.getAccountTypeName(typeId);
      BigDecimal interestRate = dbHelper.getInterestRate(typeId);
      
      // Creating and adding the serializable object to the list
      AccTypeSer accTypeSer = new AccTypeSer(typeId, name, interestRate);
      accountTypes.add(accTypeSer);
    }
  }
  
  private void updateRoles() {
    for (int roleId : dbHelper.getRoleIds()) {
      String name = dbHelper.getRole(roleId);
      RoleSer roleSer = new RoleSer(roleId, name);
      roles.add(roleSer);
    }
  }
}
